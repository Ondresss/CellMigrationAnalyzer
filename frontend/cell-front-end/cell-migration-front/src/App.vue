<template>
  <div>
    <CellNavigation></CellNavigation>
    <SideBar @showResults="hideMain"></SideBar>
    <CellMain v-show="messageFromSideBar"></CellMain>
    <CellResults
      v-show="!messageFromSideBar"
      :images="recieved_images"
      @filterTime="handleTimeFilter"
      @reloadData="handleResultDeleted"
    ></CellResults>
  </div>
</template>

<script setup>
  import { ref,inject } from 'vue';
  import axios from 'axios';
  import CellNavigation from './components/CellNavigation.vue';
  import SideBar from './components/SideBar.vue';
  import CellMain from './components/CellMain.vue';
  import CellResults from './components/CellResults.vue';
  import dayjs from 'dayjs';
  const img_dates = ref([]);
  const messageFromSideBar = ref(true);
  const API_BASE_URL = inject('API_BASE_URL');
  const recieved_images = ref([]);
  let original = [];
  const hideMain = (message) => {
    if (message) {
      messageFromSideBar.value = !messageFromSideBar.value;
    }
    recieved_images.value = message;
    original = recieved_images.value;
    console.log(recieved_images.value);
  };

  const handleTimeFilter = (date_filter) => {
    const begin = dayjs(date_filter.begin);
    const end = dayjs(date_filter.end);

    const filtered = original.filter((val) => {
      const date = dayjs(val.date);
      return date.isAfter(begin) && date.isBefore(end);
    });
    recieved_images.value = filtered;
  };
  const getDate = async (id) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/getDate?result=result${id}`);

      if (response.status === 200) {
        return response.data;
      } else {
        alert('Could not retrieve date');
        return undefined;
      }
    } catch (error) {
      console.error('Error retrieving date:', error.message);
      alert('Could not retrieve date due to an error.');
      return undefined;
    }
  };

  const getIDS = async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/resultIDS`);
      const resultIDs = response.data;
      return resultIDs;
    } catch (error) {
      console.error('Error fetching result IDs:', error);
      return [];
    }
  };

  const showLastResults = async () => {
    const res = await axios.get(`${API_BASE_URL}/results`);
    console.log(res.data);
    const ids = await getIDS();
    const dates = await Promise.all(ids.map((id_) => getDate(id_)));
    const output = res.data.map((obj, index) => ({
      files: obj.files64,
      csvNames: obj.csvFileNames,
      date: dayjs(dates[index]).format('MMMM D, YYYY h:mm:ss A'),
      fileNames: obj.fileNames,
      id: ids[index],
      currentIndex: 0
    }));

    console.log('Loaded IDS: ', ids);
    console.log('Output: ', output);
    recieved_images.value = output;
  };

  const handleResultDeleted = () => {
    showLastResults();
  };
</script>

<style>
  body {
    overflow-y: hidden;
  }
</style>

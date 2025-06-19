<template>
  <div class="sidebar-menu">
    <Menu class="men" />
    <aside class="absolute right-0 top-20 w-70 h-screen bg-gray-800 text-white p-4">
      <ul>
        <li class="mt-2">
          <button @click="showLastResults" class="last">
            <span class="ic">
            {{ t('sidebar.uploadData') || 'Upload data' }}
            </span>
          </button>
        </li>
        <li class="mt-10">
          <button @click="showLastResults" class="last">
            <span class="ic">
              {{ t('sidebar.getLastResults') || 'Get last results' }}
            </span>
          </button>
        </li>
      </ul>
    </aside>
  </div>
</template>

<script setup>
  import { defineEmits, ref, watch, inject } from 'vue';
  import axios from 'axios';
  import dayjs from 'dayjs';
  import { HomeIcon, ChartBarIcon, CogIcon, Bars3Icon } from '@heroicons/vue/24/solid';
  import { useI18n } from 'vue-i18n';

  const { t, locale } = useI18n();
  const img_resp = ref([]);
  const emit = defineEmits(['showResults']);

  const API_BASE_URL = inject('API_BASE_URL');

  locale.value = localStorage.getItem('lang') || 'en';
  // ✅ Sleduj změny v `locale` a loguj hodnoty
  watch(locale, (newVal) => {
    console.log('🌍 Aktuální jazyk:', newVal);
    locale.value = newVal;
  });

  const getDate = async (id) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/getDate?result=result${id}`);
      return response.status === 200 ? response.data : undefined;
    } catch (error) {
      console.error('Error retrieving date:', error.message);
      return undefined;
    }
  };

  const getIDS = async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/resultIDS`);
      return response.data;
    } catch (error) {
      console.error('Error fetching result IDs:', error);
      return [];
    }
  };

  const showLastResults = async () => {
    const res = await axios.get(`${API_BASE_URL}/results`);
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

    img_resp.value = output;
    emit('showResults', img_resp.value);
  };
</script>

<style scoped>
  @import '../styles/SideBar.css';
</style>

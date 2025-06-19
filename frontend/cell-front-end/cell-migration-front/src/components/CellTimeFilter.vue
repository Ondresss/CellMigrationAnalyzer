<template>
    <div class="time-filter">
        <ChevronDownIcon @click="()=>show_filter=!show_filter" class="ic w-10 h-10 text-red-500" />
        <p>Search for specific result by time of creation</p>
        <div v-show="show_filter">
            <input v-model="begin" type="datetime-local" required>
            <input v-model="end" type="datetime-local" required>
            <button @click="send_filtered_time" class="time-btn">SEARCH</button>
        </div>
    </div>
</template>

<script setup>
  import { defineEmits,ref } from 'vue';
  import { ChevronDownIcon } from "@heroicons/vue/24/solid";
  const emit = defineEmits(['timeData']);
  const begin = ref(null);
  const end = ref(null);
  const show_filter = ref(false);
  const send_filtered_time = () => {
        const date_filter = {};
        date_filter.begin = begin.value;
        date_filter.end = end.value;
        emit('timeData',date_filter);
  }

</script>

<style scoped>
  .ic {
    float: right;
  }
  .ic:hover {
    cursor: pointer;
    transform: scale(1.25);
  }
  .time-filter {
    width: 50%;
    padding: 24px; /* Space inside the box */
    border-radius: 16px; /* Rounded corners */
    background: linear-gradient(to bottom right, #2196f3, #1e88e5); /* Blue gradient */
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2); /* Smooth shadow */
    text-align: center; /* Center align text */
    color: white; /* White text for contrast */
    z-index: 4000;
    height: 2%;
    margin-bottom: 5%;
  }
  
  .time-filter p {
    margin-bottom: 16px; /* Space below the paragraph */
    font-size: 18px; /* Larger font size for the title */
    font-weight: bold; /* Make it bold */
  }
  
  .time-filter input {
    width: 50%; /* Full width of container */
    padding: 10px; /* Space inside inputs */
    margin-bottom: 12px; /* Space between inputs */
    border: none; /* Remove borders */
    border-radius: 8px; /* Rounded corners for inputs */
    background-color: #bbdefb; /* Light blue background */
    color: #0d47a1; /* Dark blue text */
    font-size: 14px; /* Font size */
    outline: none; /* Remove focus outline */
    transition: box-shadow 0.3s ease; /* Smooth shadow transition */
  }
  
  .time-filter input:focus {
    box-shadow: 0 0 8px rgba(33, 150, 243, 0.6); /* Highlight input on focus */
  }
  
  .time-filter .time-btn {
    padding: 10px; /* Space inside button */
    background-color: #1e88e5; /* Blue button */
    color: white; /* White text */
    border: none; /* Remove borders */
    border-radius: 8px; /* Rounded button corners */
    font-size: 16px; /* Font size */
    font-weight: bold; /* Bold text */
    cursor: pointer; /* Pointer cursor */
    transition: background-color 0.3s ease, transform 0.2s ease; /* Smooth transitions */
  }
  
  .time-filter .time-btn:hover {
    background-color: #1565c0; /* Darker blue on hover */
    transform: translateY(-2px); /* Lift button slightly on hover */
  }
  
  .time-filter .time-btn:active {
    transform: translateY(0); /* Reset button position when clicked */
  }
  
</style>
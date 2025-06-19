<template>
    <div class="z-[400000] bg-blue-500 border border-blue-400 text-white px-4 py-2 rounded-xl shadow-md flex items-center">
      <h3 class="text-lg font-semibold">Show only complete results</h3>
      <input
        class="ml-4 w-5 h-5 accent-blue-600"
        name="check"
        type="checkbox"
        v-model="isChecked"
      />
    </div>
  </template>
  
  <script setup>
  import { ref, watch, defineEmits } from 'vue';
  
  const emit = defineEmits(['updateChecked']);
  const isChecked = ref(false);
  
  watch(isChecked, (newVal) => {
    emit('updateChecked', newVal);
  });
  </script>
  
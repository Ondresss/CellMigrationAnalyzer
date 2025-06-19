<template>
  <div class="info-box-place">
    <InformationCircleIcon class="icon" />
    <p><slot></slot></p>
  </div>
</template>

<script setup>
  import { InformationCircleIcon } from '@heroicons/vue/24/solid';
</script>

<style scoped>
  @import '../styles/CellInfo.css';
</style>

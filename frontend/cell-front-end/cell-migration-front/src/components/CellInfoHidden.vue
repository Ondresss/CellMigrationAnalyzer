<template>
  <div>
    <p>
      <span><InformationCircleIcon class="icon" /></span><slot></slot>
    </p>
  </div>
</template>

<script setup>
  import { InformationCircleIcon } from '@heroicons/vue/24/solid';
</script>

<style lang="css" scoped>
  .icon {
    width: 5%;
    display: inline;
    color: rgb(60, 142, 170);
  }
</style>

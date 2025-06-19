<template>
  <header>
    <nav class="navigator h-20 bg-blue-500 p-4 flex shadow-lg justify-between items-center">
      <div class="text-white text-4xl font-bold">
        <h1>{{ t('appTitle') }}</h1>
      </div>
      <div class="flex items-center gap-4">
        <button @click="setLanguage('en')" class="bg-white text-black px-4 py-2 rounded">
          🇬🇧 English
        </button>
        <button @click="setLanguage('cs')" class="bg-white text-black px-4 py-2 rounded">
          🇨🇿 Čeština
        </button>
      </div>
    </nav>
  </header>
</template>

<script setup>
  import { useI18n } from 'vue-i18n';

  const { t, locale } = useI18n();

  const setLanguage = (lang) => {
    locale.value = lang;
    localStorage.setItem('lang', lang);
    location.reload(); // Obnoví stránku, aby se načetl nový JSON soubor
  };
</script>
<style scoped>
  .navigator {
    border-bottom: 1px solid rgb(125, 210, 255);
  }
</style>

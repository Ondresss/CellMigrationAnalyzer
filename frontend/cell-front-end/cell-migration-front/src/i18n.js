import { createI18n } from 'vue-i18n';
import en from './locales/en.json';
import cs from './locales/cs.json';

const messages = { en, cs };

const i18n = createI18n({
  flatJson: true,
  globalInjection: true, // Povolit `t()` globálně
  locale: localStorage.getItem('lang') || 'en',
  fallbackLocale: 'en',
  messages
});

// ✅ Aktualizuj jazyk při změně `localStorage`
window.addEventListener("storage", () => {
  const newLang = localStorage.getItem("lang");
  if (newLang && newLang !== i18n.global.locale.value) {
    console.log("📌 Změna jazyka v localStorage:", newLang);
    i18n.global.locale.value = newLang;
  }
});

export default i18n;

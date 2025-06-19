import './index.css';
import { createApp } from 'vue'
import App from './App.vue'
import axios from './plugins/axios';
import dayjs from "dayjs";
import i18n from './i18n'
import { createVuetify } from 'vuetify'
import 'vuetify/styles'
import vue from '@heroicons/vue';

const vuetify = createVuetify()


const API_BASE_URL = `${window.location.protocol}//${window.location.hostname}:8080`; 

const app = createApp(App);
app.use(i18n);
app.use(vuetify);
app.provide('API_BASE_URL', API_BASE_URL);
app.config.globalProperties.$axios = axios;
app.config.globalProperties.$dayjs = dayjs;
app.mount("#app")
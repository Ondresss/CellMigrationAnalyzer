<template>
  <div >
    <Loading
      :active="isLoading"
      :is-full-page="true"
      :text="t('processing_request')"
      :background="'rgba(0.5, 0.5, 0.5, 0.9)'"
    />
    <h2 v-show="isLoading" class="loading-text">{{ t(loading_event) }}</h2>
    <div v-show="show_info.viewLevelsets" class="show-generated-result">
      <XMarkIcon @click="() => (show_info.viewLevelsets = false)" class="closing-icon"></XMarkIcon>
      <div class="slider-container">
        <button class="prev" @click="scrollLeft">❮</button>
        <ul class="image-slider">
          <li v-for="(gen, index) in generated_response" :key="index" class="slide">
            <img :src="gen.file" :alt="t('generated_levelset')" />
            <figure>{{ gen.fileName }}</figure>
          </li>
        </ul>
        <button class="next" @click="scrollRight">❯</button>
      </div>
    </div>

    <div v-show="confirm_cell_picking" class="confirm-picking">
      <p>
        {{ t('chosen_size') }}:
        <span> {{ segment_parameters.no_box_x }}x{{ segment_parameters.no_box_y }} </span>
      </p>
      <br />
      <button @click="get_picked_data" class="yes">{{ t('confirm') }}</button>
      <button @click="() => (confirm_cell_picking = !confirm_cell_picking)" class="no">
        {{ t('cancel') }}
      </button>
    </div>

    <div class="levelSetInfo" v-show="success_response.success">
      <XMarkIcon
        @click="() => (success_response.success = !success_response.success)"
        class="close-icon"
      ></XMarkIcon>
      <h4>{{ t(success_response.message) }}</h4>
    </div>

    <div
      v-show="failure_response.failure"
      class="fixed ml-9  bg-red-100 border border-red-400 text-red-700 px-6 py-4 rounded-xl shadow-lg flex items-start gap-4 transition-all duration-300"
    >
      <XMarkIcon
        @click="() => (failure_response.failure = false)"
        class="w-5 h-5 mt-1 cursor-pointer text-red-700 hover:text-red-900 transition"
      />
      <div>
        <h4 class="font-semibold">{{ t(failure_response.message) }}</h4>
      </div>
    </div>


    <div v-show="show_cell_picker" class="cell-picker">
      <p>
        {{ t('chosen_size') }}:<span class="chosen-size"
          >{{ segment_parameters.no_box_x }}x{{ segment_parameters.no_box_y }}</span
        >
      </p>
      <XMarkIcon
        class="cell-picker-close"
        @click="() => (show_cell_picker = !show_cell_picker)"
      ></XMarkIcon>
    </div>
    <div class="levelSetInfo" v-show="loading_info.levelSetDone">
      <XMarkIcon
        @click="() => (loading_info.levelSetDone = !loading_info.levelSetDone)"
        class="close-icon"
      ></XMarkIcon>
      <h4>{{ t('levelsets_generated') }}</h4>
      <button @click="getLevelsetImages" class="show-levelsets-btn">
        {{ t('show_levelsets') }}
      </button>
    </div>

    <!---
    <div class="heading-div">
      <div class="upload-menu">
        <ul>
          <li
            @mouseenter="
              current_info_text =
                'Second step: Generate levelsets by uploading Brightfield image in TIF format'
            "
          >
            <h3>{{ t('generate_levelsets') }}</h3>
            <button @click="() => (menu_show = 0)">{{ t('choose') }}</button>
          </li>
          <li>
            <h3>{{ t('generate_all') }}</h3>
            <button @click="() => (menu_show = 1)">{{ t('choose') }}</button>
          </li>
          <li
            @mouseenter="
              current_info_text =
                'Third step: Generating final segmentated image with individual cell sectors'
            "
          >
            <h3>{{ t('generate_segmented_result') }}</h3>
            <button @click="() => (menu_show = 2)">{{ t('choose') }}</button>
          </li>
          <li
            @mouseenter="
              current_info_text =
                'First step: Upload DAPI images for visualisation and computation thats used in next steps'
            "
          >
            <h3>{{ t('upload_dapi_images') }}</h3>
            <button @click="() => (menu_show = 3)">{{ t('choose') }}</button>
          </li>
        </ul>
        <CellInfo>{{ current_info_text }}</CellInfo>
      </div>
    </div>
  -->
  <div class="mt-5 flex gap-4 p-4">
    <!-- Left section with CellStepper -->
    <div class="flex-1 flex items-center justify-center p-6 rounded-lg shadow-md">
      <CellStepper @increase="increment" class="h-80 w-100 " />
    </div>
  </div>

  <div class="absolute mt-15 top-20 left-10">
  <div v-show="menu_show == 1" class="p-6 rounded-lg shadow-md">
  <h2 class="text-lg font-semibold text-gray-700">{{ t('upload_dapi_images') }}</h2>
  <input type="file" multiple @change="pickDAPIFiles" class="block w-full mt-2 p-2 border border-gray-300 rounded-md focus:ring focus:ring-blue-300" />
  <button @click="handleDAPIUpload" class="mt-4 w-full bg-blue-500 text-white py-2 rounded-md hover:bg-blue-600 transition">
    {{ t('upload_dapi') }}
  </button>
</div>
<div v-show="menu_show==0" class="mt-10 p-6 bg-white rounded-2xl shadow-lg space-y-6 w-full max-w-lg mx-auto">
  <!-- Toggle Switch -->
  <div class="flex items-center justify-between">
    <label for="resultChoose" class="text-base font-medium text-gray-800">
      {{ t('generate_new_result') }}
    </label>
    <label class="relative inline-flex items-center cursor-pointer">
      <input v-model="genNewResult" type="checkbox" id="resultChoose" class="sr-only peer">
      <div class="w-12 h-6 bg-gray-300 peer-focus:ring-2 peer-focus:ring-blue-400 rounded-full peer peer-checked:after:translate-x-6 peer-checked:after:bg-white peer-checked:bg-blue-600 after:content-[''] after:absolute after:top-1 after:left-1 after:bg-white after:border after:rounded-full after:h-4 after:w-4 after:transition-all shadow-md"></div>
    </label>
  </div>

  <!-- Select Existing Result -->
  <div>
    <label class="block text-base font-medium text-gray-800">
      {{ t('add_to_existing_result') }}
    </label>
    <select v-model="selected_id" @mouseenter="loadResultIDS" class="w-full mt-2 p-3 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none transition">
      <option value="" disabled selected>{{ t('choose_existing_result') }}</option>
      <option v-for="(res_id, index) in loaded_ids" :value="res_id" :key="index">
        #{{ res_id }}
      </option>
    </select>
  </div>

  <!-- Action Buttons -->
  <div class="flex justify-end space-x-4">
    <button class="px-5 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400 transition">Cancel</button>
    <button class="px-5 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition">Confirm</button>
  </div>
</div>



<div v-show="menu_show == 3" class="bg-white p-6 rounded-lg shadow-md">
  <h2 class="text-lg font-semibold text-gray-700">{{ t('upload_segmented_data') }}</h2>
  
  <div class="mt-4 space-y-4">
    <div>
      <label class="block font-medium text-gray-600">{{ t('add_spots_csv') }}</label>
      <input @change="getSpots" type="file" class="block w-full p-2 border border-gray-300 rounded-md focus:ring focus:ring-blue-300" />
    </div>
    <div>
      <label class="block font-medium text-gray-600">{{ t('add_tracks_csv') }}</label>
      <input @change="getTracks" type="file" class="block w-full p-2 border border-gray-300 rounded-md focus:ring focus:ring-blue-300" />
    </div>
    <div>
      <label class="block font-medium text-gray-600">{{ t('num_brightfields') }}</label>
      <input v-model="segment_parameters.no_iter" min="0" max="70" type="number" class="block w-full p-2 border border-gray-300 rounded-md focus:ring focus:ring-blue-300" />
    </div>
    <div>
      <label class="block font-medium text-gray-600">{{ t('step_size') }}: {{ segment_parameters.step_size }}</label>
      <input v-model="segment_parameters.step_size" type="range" class="w-full accent-blue-500" />
    </div>
  </div>

  <button @click="startSegmentation" class="mt-4 w-full bg-green-500 text-white py-2 rounded-md hover:bg-green-600 transition">
    {{ t('upload_data') }}
  </button>
</div>

<div v-show="menu_show == 2" class="bg-white p-6 rounded-lg shadow-md">
  <h2 class="text-lg font-semibold text-gray-700">{{ t('upload_brightfield') }}</h2>
  <input name="upload" accept=".tif" type="file" @change="pickFile" multiple class="block w-full p-2 border border-gray-300 rounded-md focus:ring focus:ring-blue-300" />

  <div class="mt-4">
    <span class="block font-medium text-gray-600">{{ t('num_iterations') }}: {{ value }}</span>
    <input type="range" v-model="value" min="0" max="500" step="1" class="w-full accent-blue-500" />
  </div>

  <button class="mt-4 w-full bg-blue-500 text-white py-2 rounded-md hover:bg-blue-600 transition" @click="uploadBrightField">
    {{ t('upload') }}
  </button>
</div>
</div>
  </div>
</template>

<script setup>
  import { ref, watch, inject} from 'vue';
  import { useI18n } from 'vue-i18n';
  import axios from 'axios';
  import CellInfo from './CellInfo.vue';
  import CellStepper from './CellStepper.vue';
  import { XMarkIcon } from '@heroicons/vue/24/outline';
  const selected_brightfield = ref([]);
  const selected_dapis = ref([]);
  const value = ref(0);
  import Loading from 'vue-loading-overlay';
  import 'vue-loading-overlay/dist/css/index.css';
  const loading_event = ref('');
  const genNewResult = ref(false);
  const loaded_ids = ref([]);
  const selected_id = ref('');

  const current_info_text = ref(
    'Here you can generate full result step by step ( Hover on each button for more)'
  );

  const increment = (value) => {
      menu_show.value = value;
  }

  const segment_parameters = ref({
    no_iter: 0,
    step_size: 0,
    no_box_y: 'Not selected',
    no_box_x: 'Not selected'
  });
  const trackmate_csv = ref({
    spots: null,
    tracks: null
  });
  const actual_no_boxes = ref({
    x: 'Not selected',
    y: 'Not selected'
  });
  const show_cell_picker = ref(false);
  const loading_info = ref({
    levelSetDone: false
  });
  const success_response = ref({
    success: false,
    message: 'Successful'
  });
  const show_info = ref({
    viewLevelsets: false
  });
  const failure_response = ref({
    failure: false,
    message: 'Segmentation failed'
  });
  const cell_picking_canvas = ref(null);
  const menu_show = ref(0);
  const generated_response = ref([]);
  const isLoading = ref(false);
  const pickFile = (event) => {
    const files = Array.from(event.target.files);
    for (const f of files) {
      if (!f.name.endsWith('.tif')) {
        alert('Onlu accepting .tif format');
        event.target.value = '';
      }
    }
    selected_brightfield.value = files;
  };
  const pickDAPIFiles = (e) => {
    selected_dapis.value = Array.from(e.target.files);
  };

  const getSpots = (e) => {
    const file = e.target.files[0];
    if (!file.name.endsWith('.csv')) {
      e.target.value = '';
      alert('Please choose only csv files');
      return;
    }
    trackmate_csv.value.spots = e.target.files[0];
  };

  const getTracks = (e) => {
    const file = e.target.files[0];
    if (!file.name.endsWith('.csv')) {
      e.target.value = '';
      alert('Please choose only csv files');
      return;
    }
    trackmate_csv.value.tracks = e.target.files[0];
  };

  const limits = ref({
    horizontal_boxes: 20,
    vertical_boxes: 20
  });
  const confirm_cell_picking = ref(false);

  let lastID = null;

  const { t, locale } = useI18n();

  locale.value = localStorage.getItem('lang') || 'en';
  watch(locale, (newVal) => {
    console.log('🌍 Aktuální jazyk:', newVal);
    locale.value = newVal;
  });

  const API_BASE_URL = inject('API_BASE_URL');

  const show_user_cell_picking = () => {
    show_cell_picker.value = !show_cell_picker.value;
    const picking_place = document.querySelector('.cell-picker');
    const exists_canvas = picking_place.querySelector('canvas');
    if (exists_canvas) {
      exists_canvas.remove();
    }
    let created_canvas = null;
    if (picking_place) {
      created_canvas = document.createElement('canvas');
      created_canvas.style.width = '100%';
      created_canvas.style.height = '40%';
      created_canvas.style.border = '1px solid black';
      picking_place.appendChild(created_canvas);
    }
    cell_picker_canvas_init(created_canvas);
  };

  const get_picked_data = () => {
    confirm_cell_picking.value = !confirm_cell_picking.value;
    show_cell_picker.value = !show_cell_picker.value;
  };

  const loadResultIDS = async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/resultIDS`);
      if (response.status === 200) {
        loaded_ids.value = response.data;
        console.log(loaded_ids.value);
      } else {
        alert('Could not retrieve date');
      }
    } catch (error) {
      console.error('Error retrieving date:', error.message);
      return undefined;
    }
  };

  const cell_picker_canvas_init = (canvas) => {
    const ctx = canvas.getContext('2d');
    const CANVAS_WIDTH = canvas.width;
    const CANVAS_HEIGHT = canvas.height;
    const NO_BOXES_Y = CANVAS_HEIGHT / limits.value.vertical_boxes;
    const NO_BOXES_X = CANVAS_WIDTH / limits.value.vertical_boxes;
    ctx.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
    cell_picker_draw_matris(ctx, CANVAS_WIDTH, CANVAS_HEIGHT);
    canvas.addEventListener('mousemove', (e) =>
      handle_picker_mouse_move(e, canvas, ctx, NO_BOXES_X, NO_BOXES_Y)
    );
    canvas.addEventListener('click', (e) =>
      handle_picker_click(e, canvas, ctx, NO_BOXES_X, NO_BOXES_Y)
    );
  };
  const handle_picker_mouse_move = (e, canvas, ctx, NO_BOXES_X, NO_BOXES_Y) => {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    const actual_no_boxes_x = Math.ceil((e.offsetX * 0.5) / NO_BOXES_X);
    const actual_no_boxes_y = Math.ceil((e.offsetY * 0.5) / NO_BOXES_Y);
    for (let r = 0; r < actual_no_boxes_y; r++) {
      for (let c = 0; c < actual_no_boxes_x; c++) {
        ctx.fillStyle = 'blue';
        ctx.fillRect(c * NO_BOXES_X, r * NO_BOXES_Y, NO_BOXES_X, NO_BOXES_Y);
        segment_parameters.value.no_box_x = r + 1;
        segment_parameters.value.no_box_y = c + 1;
      }
    }
    cell_picker_draw_matris(ctx, canvas.width, canvas.height);
  };

  const handle_picker_click = (e, canvas, ctx, NO_BOXES_X, NO_BOXES_Y) => {
    const actual_no_boxes_x = Math.ceil((e.offsetX * 0.5) / NO_BOXES_X);
    const actual_no_boxes_y = Math.ceil((e.offsetY * 0.5) / NO_BOXES_Y);
    for (let r = 0; r < actual_no_boxes_y; r++) {
      for (let c = 0; c < actual_no_boxes_x; c++) {
        ctx.fillStyle = 'green';
        ctx.fillRect(c * NO_BOXES_X, r * NO_BOXES_Y, NO_BOXES_X, NO_BOXES_Y);
        segment_parameters.value.no_box_x = r + 1;
        segment_parameters.value.no_box_y = c + 1;
      }
    }
    cell_picker_draw_matris(ctx, canvas.width, canvas.height);
    confirm_cell_picking.value = !confirm_cell_picking.value;
  };

  const cell_picker_draw_matris = (ctx, CANVAS_WIDTH, CANVAS_HEIGHT) => {
    const NO_BOXES_Y = CANVAS_HEIGHT / limits.value.vertical_boxes;
    const NO_BOXES_X = CANVAS_WIDTH / limits.value.vertical_boxes;
    for (let r = 0; r < limits.value.vertical_boxes; r++) {
      for (let c = 0; c < limits.value.horizontal_boxes; c++) {
        ctx.strokeStyle = 'black';
        ctx.strokeRect(r * NO_BOXES_X, c * NO_BOXES_Y, NO_BOXES_X, NO_BOXES_Y);
      }
    }
  };

  const handleDAPINew = async () => {
    try {
      const formData = new FormData();
      selected_dapis.value.forEach((file) => {
        formData.append('files', file);
      });

      const response = await axios.post(`${API_BASE_URL}/uploadDapiNEW`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      if (response.status === 200) {
        success_response.value.success = !success_response.value.success;
        success_response.value.message = 'Uploaded new DAPI and created new result successfully';
        lastID = Number(response.data);
      } else {
        alert('Could not retrieve date');
      }
    } catch (error) {
      console.error('Error retrieving date:', error.message);
    }
  };

  const handleDAPIExisting = async () => {
    try {
      const formData = new FormData();
      if (selected_id.value === '' || selected_id.value === undefined) {
        alert('Please choose result id');
        return;
      }
      formData.append('ID', selected_id.value);
      selected_dapis.value.forEach((val) => {
        formData.append('files', val);
      });
      const response = await axios.post(`${API_BASE_URL}/uploadDapiExisting`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      if (response.status === 200) {
        success_response.value.success = !success_response.value.success;
        success_response.value.message = `Uploaded new DAPI to result #${selected_id.value}`;
      } else {
        alert('Could not retrieve date');
      }
    } catch (error) {
      console.error('Error retrieving date:', error.message);
      return undefined;
    }
  };

  const handleDAPIUpload = async () => {
    if (genNewResult.value) {
      await handleDAPINew();
    } else {
      await handleDAPIExisting();
    }
  };

  const startSegmentation = async () => {
    try {
      let formData = new FormData();
      let concrete_id = null;
      if (!selected_id.value || selected_id.value.trim() === '') {
          if(!lastID) {
             alert('Please choose result id');
             return;
          } else {
              concrete_id = lastID;
          }
      } else {
          concrete_id = selected_id.value;
      }

      if (!trackmate_csv.value || !trackmate_csv.value.spots || !trackmate_csv.value.tracks) {
        alert('Invalid trackmate data');
        return;
      }
      if(concrete_id == null) {
          alert('Please choose result id');
          return;
      }
      alert(concrete_id);
      formData.append('ID', concrete_id);
      formData.append('spots', trackmate_csv.value.spots);
      formData.append('tracks', trackmate_csv.value.tracks);

      const response = await axios.post(`${API_BASE_URL}/api/prepareSegmentation`, formData);

      if (response.status === 200) {
        console.log('Tracks and spots uploaded successfully');

        if (
          !segment_parameters.value ||
          segment_parameters.value.no_iter === undefined ||
          segment_parameters.value.step_size === undefined ||
          segment_parameters.value.no_box_x === undefined ||
          segment_parameters.value.no_box_y === undefined
        ) {
          alert('Segmentation parameters are missing.');
          return;
        }
        loading_event.value = 'Creating tracks and data....';
        isLoading.value = true;
        const stream = new EventSource(
          `${API_BASE_URL}/api/segmentResult?ID=${concrete_id}&no_iter=${segment_parameters.value.no_iter}&step_size=${segment_parameters.value.step_size}&no_box_x=${segment_parameters.value.no_box_x}&no_box_y=${segment_parameters.value.no_box_y}`
        );

        stream.onmessage = (event) => {
          console.log(event.data);
          if(event.data === 'Traceback') {
            failure_response.value.failure = true;
            failure_response.value.message = 'Segmentation failed';
            isLoading.value = false;
            stream.close();
          }
          if (event.data === 'END') {
            loading_event.value = 'Segmentation done';
            isLoading.value = false;
            loading_info.value.levelSetDone = true;
            stream.close();
          }
        };

        stream.onerror = (error) => {
          console.error('SSE error:', error);
          stream.close();
        };
      } else {
        console.error('Data upload failed');
      }
    } catch (error) {
      console.error('Error retrieving data:', error.message);
    }
  };

  const getLevelsetImages = async () => {
    const formData = new FormData();
    if (lastID != null) {
      formData.append('ID', lastID);
    } else {
      if(!selected_id.value || selected_id.value.trim() === '') {
        alert('Please choose result id');
        return;
      }
      formData.append('ID', selected_id.value);
    }
    const response = await axios.post(`${API_BASE_URL}/getLevelsets`, formData);
    console.log(response.data);
    generated_response.value = response.data;
    show_info.value.viewLevelsets = !show_info.value.viewLevelsets;
  };

  const handleNewResultUpload = async (formData) => {
    if(lastID == null) {
        alert('Please choose result id');
        return;
    }
    const response = await axios.post(`${API_BASE_URL}/uploadBrightField?ID=${lastID}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    console.log('Upload successful:', lastID);
    loading_event.value = 'Generating levelsets....';
    isLoading.value = true;
    const eventSource = new EventSource(
      `${API_BASE_URL}/api/generateLevelSets?no_iter=${value.value}&ID=${lastID}`
    );
    eventSource.onmessage = (event) => {
      console.log(event.data);
      if (event.data == 'END') {
        isLoading.value = false;
        loading_info.value.levelSetDone = true;
      }
    };

    eventSource.onerror = () => {
      console.log('SSE error, closing connection.');
      eventSource.close();
    };
  };

  const handleExistingUpload = async (formData) => {
    formData.append('ID', selected_id.value);
    const response = await axios.post(`${API_BASE_URL}/existingBrightFieldResult`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });

    if (response.status == 200) {
      console.log('Upload to existin result: ', selected_id.value);
    } else {
      console.error('Upload failed to existing result');
      return;
    }

    loading_event.value = 'Generating levelsets to existing result....';
    isLoading.value = true;
    const eventSource = new EventSource(
      `${API_BASE_URL}/generateNewBrightFields?no_iter=${value.value}&ID=${selected_id.value}`
    );
    eventSource.onmessage = (event) => {
      console.log(event.data);
      if (event.data == 'END') {
        isLoading.value = false;
        loading_info.value.levelSetDone = true;
      }
    };

    eventSource.onerror = () => {
      console.log('SSE error, closing connection.');
      eventSource.close();
    };
  };
  const uploadBrightField = async () => {
    if (selected_brightfield.value.length === 0) {
      console.log('No files selected.');
      return;
    }
    const formData = new FormData();
    selected_brightfield.value.forEach((file) => {
      formData.append('files', file);
    });

    try {
      if (genNewResult.value) {
        handleNewResultUpload(formData);
      } else {
        handleExistingUpload(formData);
      }
    } catch (error) {
      console.error('Upload failed:', error);
    }
  };
</script>

<style scoped>
  @import '../styles/CellMain.css';
</style>

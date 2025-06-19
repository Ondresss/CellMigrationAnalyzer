<template>
  <div class="container">
    <aside
      :style="{ top: scratchTop }"
      ref="scratchEl"
      v-show="show_scratch_info.show"
      class="scratch-info"
    >
      <p>SCRATCH WIDTH: {{ show_scratch_info.width }} {{ current_unit }}</p>
    </aside>
    <div v-show="show_scratch_info.info" style="z-index: 100000;" class="absolute top-20 left-20 w-2/3 h-2/3 bg-white/90 rounded-2xl shadow-xl p-6 overflow-y-auto backdrop-blur-md border border-gray-300">
  <article class="text-sm text-gray-700 leading-relaxed space-y-2">
    <p><strong>x</strong> – označení sektoru na ose x, box (sektor) vlevo nahoře je [0,0]</p>
    <p><strong>y</strong> – označení sektoru na ose y</p>
    <p><strong>center_x</strong> – středový bod na ose x boxu v jednotkách px (pixels)</p>
    <p><strong>center_y</strong> – středový bod na ose y boxu v jednotkách px (pixels)</p>
    <p><strong>avg_top_cells_vec_x</strong> – průměrná rychlost top-cells ve směru osy x</p>
    <p><strong>avg_top_cells_vec_y</strong> – průměrná rychlost top-cells ve směru osy y</p>
    <p><strong>top-cells</strong> – počet tzv. top-cells sektoru</p>
    <p><strong>avg_vec_x</strong> – průměrná rychlost všech buněk v jednom boxu ve směru osy x</p>
    <p><strong>avg_vec_y</strong> – průměrná rychlost všech buněk v jednom boxu ve směru osy y</p>
    <p><strong>count</strong> – celkový počet buněk v jednom boxu</p>
    <p><strong>avg_normal_vec_x</strong> – průměrný normálový vektor x</p>
    <p><strong>avg_normal_vec_y</strong> – průměrný normálový vektor y</p>
    <p><strong>angle</strong> – úhel mezi vec_x a vec_y</p>
    <p><strong>box_boundary_distance</strong> – vzdálenost sektoru (boxu) od hranice rány</p>
  </article>
</div>

    <aside v-show="show_approximated_width" class="scratch-width-approximation">
      <p>
        SCRATCH AREA APPROXIMATION:
        <span>{{ current_total_scratch_width }} {{ current_unit }}<sup>2</sup></span>
      </p>
    </aside>

    <div v-show="show_scratch_info.graph" class="scratch-width-visualisation">
      <XMarkIcon
        @click="() => (show_scratch_info.graph = false)"
        class="w-10 h-10 text-white-500 cursor-pointer"
      />
    </div>

    <div v-show="show_visualizer" class="data-visualizer">
      <div @click="hideVisualizer" class="visualizer-menu">
        <button class="bg-blue-400"><ArrowLeftIcon class="w-20 h-10 text-white-500" /></button>
      </div>
      <div class="boundary-distance-graph"></div>
      <div class="graph-menu p-4 bg-white shadow-md rounded-lg flex flex-col gap-2">
        <label for="graph" class="text-sm font-medium text-gray-700">
          Choose values for next graph:
        </label>
        <div class="flex gap-2">
          <select
            v-model="graph_param"
            name="graph"
            class="w-full p-2 border border-gray-300 rounded-md focus:ring focus:ring-blue-200"
          >
            <option value="" disabled selected>-- Select a graph parameter --</option>
            <option :value="name" v-for="(name, index) in csv_headers" :key="index">
              {{ name }}
            </option>
          </select>

          <select
            v-model="graph_type"
            name="graph-type"
            class="w-full p-2 border border-gray-300 rounded-md focus:ring focus:ring-blue-200"
          >
            <option value="" disabled selected>-- Select graph type--</option>
            <option value="line">Line</option>
            <option value="bar">Bar</option>
            <option value="pie">Pie</option>
          </select>
          <button 
              @click="change_graph"
              class="px-5 py-2 rounded-xl bg-blue-600 text-white font-semibold shadow-md hover:bg-blue-700 transition duration-200"
            >
              CHOOSE
          </button>

        </div>
      </div>
    </div>
    <div
      class="comment-section bg-gray-800 p-6 rounded-lg shadow-lg"
      :class="{ show: isCommentVisible }"
    >
      <div class="mb-6">
        <h3 class="text-lg font-semibold text-white mb-3">Add a Comment</h3>

        <label for="author" class="block text-gray-400 mb-1">Author Name</label>
        <input
          type="text"
          id="author"
          class="w-full px-4 py-2 mb-4 text-white bg-gray-700 rounded-lg border border-gray-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          placeholder="Enter your name"
          v-model="new_comment.author"
        />

        <label for="comment" class="block text-gray-400 mb-1">Your Comment</label>
        <textarea
          id="comment"
          rows="4"
          class="w-full px-4 py-2 text-white bg-gray-700 rounded-lg border border-gray-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          placeholder="Write your comment..."
          v-model="new_comment.text"
        ></textarea>

        <button
          @click="submitComment"
          class="mt-4 w-full bg-blue-500 hover:bg-blue-600 text-white font-semibold py-2 rounded-lg shadow-md transition"
        >
          Submit Comment
        </button>
      </div>

      <div
        v-for="(com, index) in comments"
        :key="index"
        class="comment-item bg-gray-700 p-4 mb-4 rounded-lg shadow-md"
      >
        <h2 class="text-xl font-semibold text-white mb-2">{{ com.author }}</h2>
        <pre class="text-gray-300 whitespace-pre-wrap">{{ com.text }}</pre>
      </div>
    </div>

    <div v-show="dialog" class="con">
      <button class="hideCSVBTN" @click="hideCSV">
        <XMarkIcon class="w-10 h-10 text-white-500 cursor-pointer" />
      </button>
      <table class="csvResultTable border border-gray-300 rounded-lg shadow-lg overflow-hidden">
        <thead>
          <tr class="bg-blue-600 text-white uppercase text-sm leading-normal">
            <th v-for="(head, index) in csv_headers" :key="index" class="py-3 px-6 text-left">
              {{ head }}
            </th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200 overflow-y-scroll max-h-96">
          <tr
            v-for="(data, index) in csv_data"
            :key="index"
            class="hover:bg-gray-100 transition-colors duration-200"
          >
            <td v-for="(row, num) in data" :key="num" class="py-3 px-6">
              {{ row }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div class="w-full ">
    <CellTimeFilter @timeData="handle_filtered_time"></CellTimeFilter>
    <CellCompleteResults @updateChecked="handle_update_check" style="float: right;margin-top: -7%;margin-right: 20%;"></CellCompleteResults>
    <CellInfoHidden  class="information-place w-4/5">{{ information_text }}</CellInfoHidden>
    </div>
    <div v-show="!show_original" class="original-data-show">
      <div class="original-image">
        <img src="" alt="Could not load brightfield image" />
      </div>
      <input
        @input="opacitySliderChange"
        v-model="data_img_opacity"
        min="0"
        max="100"
        class="styled-range"
        type="range"
      />
    </div>
    <div
       v-for="(im, index) in !show_complete_only_results ? images.slice(
        showing_index,
        min_to_show
      ) : filtered_results.slice(
        complete_showing_index,
        min_to_show
      )"
      :key="index + (show_complete_only_results ? complete_showing_index : showing_index)"
      class="bg-white shadow-md rounded-lg p-4 mb-4 w-4/5"
      v-show="im.fileNames.length > 0 || !show_complete_only_results"
    >
      <h2 class="text-lg font-semibold text-gray-800 mb-2">Result: {{ index + 1 }}</h2>
      <p class="res-id">ID: #{{ im.id }}</p>
      <button
        :class="{ 'disabled-btn': im.fileNames.length <= 0 }"
        class="bg-blue-500 mr-3 text-white font-bold py-2 px-4 rounded hover:bg-blue-700 transition-colors duration-200"
        @click="showResult(index, im.fileNames.length)"
      >
        Show result
      </button>
      <button
        :disabled="im.fileNames.length <= 0"
        :class="{ 'disabled-btn': im.fileNames.length <= 0 }"
        class="bg-green-500 mr-3  text-white font-bold py-2 px-4 rounded hover:bg-blue-700 transition-colors duration-200"
        @click="showCSV(im.csvNames[current_result_index], im.id)"
      >
        Show CSV table
      </button>
      <button
        class="bg-red-500 mr-3  text-white font-bold py-2 px-4 rounded hover:bg-blue-700 transition-colors duration-200"
        @click="removeResult(im.id)"
      >
        Delete result
      </button>
      <button
        @click="toggleComments(im.id)"
        class="bg-purple-500 mr-3  text-white font-bold py-2 px-4 rounded hover:bg-blue-700 transition-colors duration-200"
      >
        <ChatBubbleLeftIcon
          style="display: inline"
          class="w-5 h-5 text-gray-500 hover:text-blue-500"
        />
        COMMENT
      </button>
      <aside class="created-time">
        Created: <span style="font-weight: bold">{{ im.date }}</span>
      </aside>

      <div
    v-show="show_manual_number"
    class="number-selector bg-white p-6 rounded-xl shadow-lg border border-gray-300 max-w-md mx-auto"
  >
    <!-- Header + Buttons -->
    <div class="flex items-center justify-between mb-4">
      <h4 class="text-xl font-semibold text-gray-700">{{ selected_value_filter }}</h4>
      <div class="flex gap-2">
        <button @click="hideIntervalSelector" class="bg-blue-500 hover:bg-blue-600 text-white p-2 rounded-lg">
          <ArrowLeftIcon class="w-6 h-6" />
        </button>
        <button @click="draw_selected_filter(index)" class="bg-green-400 hover:bg-green-500 text-white p-2 rounded-lg">
          <FunnelIcon class="w-6 h-6" />
        </button>
      </div>
    </div>

    <!-- From Slider -->
    <div class="mb-6">
      <label for="from" class="block text-sm font-medium text-gray-600 mb-1">From:</label>
      <input
        v-model="selected_from"
        name="from"
        type="range"
        id="slider1"
        min="-500"
        max="500"
        step="1"
        class="w-full accent-blue-600"
      />
      <p class="text-sm text-gray-500 mt-1">Current: {{ selected_from }}</p>
    </div>

    <!-- To Slider -->
    <div class="mb-6">
      <label for="to" class="block text-sm font-medium text-gray-600 mb-1">To:</label>
      <input
        v-model="selected_to"
        name="to"
        type="range"
        id="slider2"
        min="-500"
        max="500"
        step="1"
        class="w-full accent-green-600"
      />
      <p class="text-sm text-gray-500 mt-1">Current: {{ selected_to }}</p>
    </div>

    <!-- Display interval -->
    <p class="text-center text-md text-gray-700 font-semibold">
      Interval: &lt; {{ selected_from }} ; {{ selected_to }} &gt;
    </p>
  </div>
      <div
        v-show="images_show[index]"
        class="calculated-data-show bg-gray-100 rounded-lg"
        :class="['calculated-data-show bg-gray-100 rounded-lg transition-transform duration-300', { '-translate-x-8 w-50': shiftLeft }]"
      >
        <div class="select-heading">
          <button @click="handleShift" class="flex z-[1000000] mr-8 items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-xl shadow transition-all duration-200">
            <ArrowLeftIcon class="w-4 h-4" />
          </button>

          <CellInfoHidden @mouseleave="()=> show_scratch_info.info = false"  @mouseenter="()=>show_scratch_info.info = true " class="table-info" ></CellInfoHidden>
          <h3>Calculated data</h3>
        </div>
        <div class="overflow-x-auto bg-gray-100">
          <table class="w-full min-w-max rounded-lg">
            <thead class="bg-gray-800 text-white">
              <tr>
                <th class="p-3 text-left">Column</th>
                <th class="p-3 text-left">Mean</th>
                <th class="p-3 text-left">Median</th>
                <th class="p-3 text-left">STD</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(col, index) in columns"
                :key="index"
                class="odd:bg-blue-500 even:bg-blue-700 hover:bg-gray-700"
              >
                <td class="p-3 font-semibold text-white">{{ col }}</td>
                <td class="p-3 text-gray-300">{{ calculated_data[col]?.mean ?? 'N/A' }}</td>
                <td class="p-3 text-gray-300">{{ calculated_data[col]?.median ?? 'N/A' }}</td>
                <td class="p-3 text-gray-300">{{ calculated_data[col]?.std ?? 'N/A' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div class="selector-menu p-4 bg-gray-100 rounded-lg shadow-md" v-show="images_show[index]">
        <div class="select-heading">
          <h3>Selection menu</h3>
        </div>
        <div class="max-w-sm mx-auto mt-4">
          <select
            v-model="selectMode"
            id="styled-select"
            name="options"
            class="block w-full px-4 py-2 bg-white border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
          >
            <option value="" disabled selected>Choose box selection mode</option>
            <option value="1">Row</option>
            <option value="2">Column</option>
            <option value="3">Square</option>
            <option value="4">Single</option>
          </select>
          <div class="flex flex-wrap gap-4 mt-5 items-center">
  <select
    @change="find_boxes"
    v-model="selected_value_filter"
    name="box-filter"
    class="w-full px-4 py-2 bg-white border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
  >
    <option value="" disabled selected>Select boxes by specific value</option>
    <option :value="name" v-for="(name, index) in csv_headers" :key="index">
      {{ name }}
    </option>
  </select>


  <select
    v-model="current_unit"
    name="from-units"
    class="w-1/4 min-w-[100px] px-4 py-2 bg-white border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
  >
    <option :value="current_unit" disabled selected>From unit</option>
    <option :value="current_unit">{{ current_unit }}</option>
  </select>

  <span>to</span>

  <select
    v-model="to_unit"
    name="to-units"
    class="w-2/4 px-4 py-2 bg-white text-center border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
  >
    <option value="" disabled selected>To unit</option>
    <option value="Px">Px</option>
    <option value="Mm">Mm</option>
    <option value="Cm">Cm</option>
  </select>
</div>

          <button
            @click="showDataImage(im.id)"
            :class="show_original ? 'originalBTN' : 'originalBTNHide'"
          >
            {{ original_button_text }}
          </button>
          <button @click="precalculate_all_scratch_widths(im)" class="scratchWidthBTN">
            SCRATCH AREA CHANGE
          </button>
          <select v-model="box_indexing_form" class="w-full min-w-[100px] px-4 py-2 mt-5 bg-white border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-center">
            <option value="" disabled selected>Choose box indexing</option>
            <option value="1">Row based</option>
            <option value="2">Column based</option>
          </select>

          <div
            class="result-switch flex items-center justify-between w-40 mx-auto p-2 bg-gray-100 rounded-lg shadow-lg"
          >
            <button
              @click="switch_result(im.files, false, index, im.csvNames, im.id)"
              class="p-2 hover:bg-gray-200 rounded-full transition"
            >
              <ArrowLeftIcon class="w-6 h-6 text-gray-600" />
            </button>
            <span class="text-gray-600 font-semibold text-sm"
              >{{ current_result_index + 1 }}/{{ im.fileNames.length }}</span
            >
            <button
              @click="switch_result(im.files, true, index, im.csvNames, im.id)"
              class="p-2 hover:bg-gray-200 rounded-full transition"
            >
              <ArrowRightIcon class="w-6 h-6 text-gray-600" />
            </button>
          </div>
          <button
            @click="downloadImage(im.files, im.fileNames)"
            class="downloadBTN flex items-center px-4 py-2 text-white bg-blue-600 rounded-lg hover:bg-blue-700"
          >
            <ArrowDownTrayIcon class="w-5 h-5 mr-2" />
            Download Image
          </button>
        </div>
      </div>

      <div class="img_menu" v-show="images_show[index]">
        <div class="info-box bg-gray-100 rounded-lg shadow-md">
          <button
            class="bg-red-500 text-white font-bold py-2 px-4 rounded hover:bg-blue-700 transition-colors duration-200"
            @click="showResult(index)"
          >
            <XMarkIcon class="w-6 h-6 text-white-500 cursor-pointer" />
          </button>
          <p v-if="box_indexing_form==='1'"><span>Box index: </span>{{ isNaN(convertIndex(Math.ceil(box_index / no_boxes.cols),box_index % no_boxes.cols,no_boxes.rows)) ? 'Not specified' : convertIndex(Math.ceil(box_index / no_boxes.cols),box_index % no_boxes.cols,no_boxes.rows)  }}</p>
          <p v-else><span>Box index: </span>{{ isNaN(box_index) ? 'Not specified' : box_index }}</p>
          <p class="l">
            <span>PosX: {{ pos_x }}</span>
          </p>
          <p class="r">
            <span>PosY: {{ pos_y }}</span>
          </p>
          <button
            @click="animate_results(index + (show_complete_only_results ? complete_showing_index : showing_index))"
            class="bg-green-500 text-white font-bold py-2 px-4 rounded hover:bg-blue-700 transition-colors duration-200"
          >
            <PlayIcon v-if="!show_scratch_info.playing" class="w-6 h-6 text-white-500" />
            <PauseIcon v-else class="w-6 h-6 text-white-500" />
          </button>
          <button
            :style="{ backgroundColor: excluder.excluded ? 'green' : 'black ' }"
            @click="handleExcludedClick"
            class="pencilBTN"
          >
            <CheckIcon v-if="excluder.excluded" class="w-6 h-6" />
            <PencilIcon v-else class="w-6 h-6" />
          </button>
          <aside>
            <p>
              Generated result:
              <span style="font-weight: bold; margin-left: 5%">{{
                im.fileNames[current_result_index]
              }}</span>
            </p>
          </aside>
        </div>
        <img
          @load="(event) => handleImageLoad(event, index, im.csvNames[current_result_index], im.id)"
          :class="`image${index}`"
          style="display: none"
          :src="im.files[0]"
        />
        <div v-show="show_info_box" class="tmp_box">
          <table class="border border-gray-300 rounded-lg shadow-lg overflow-hidden">
            <thead>
              <tr class="bg-blue-600 text-white uppercase text-sm leading-normal">
                <th v-for="(head, index) in csv_headers" :key="index" class="py-3 px-6 text-left">
                  {{ head }}
                </th>
              </tr>
            </thead>
            <tbody
              class="bg-white divide-y divide-gray-200 hover:bg-blue-100 hover:shadow-lg transform hover:scale-[1.02] transition-all"
            >
              <tr
                v-for="(data, index) in tmp_data"
                :key="index"
                class="hover:bg-blue-100 hover:shadow-lg transform hover:scale-[1.02] transition-all duration-200 ease-in-out"
              >
                <td v-for="(row, num) in data" :key="num" class="py-3 px-6">
                  {{ row }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div class="next-result-switcher">
      <div class="result-range">
        <p>{{ Math.min(showing_index + 3, images.length)  }} of {{ images.length }}</p>
      </div>
      <div class="arrow-keys">
        <button @click="handle_click_left" class="arrow bg-gray-800 rounded mr-2">
            <ArrowLeftIcon />
          </button>

          <button @click="handle_click_right" class="arrow bg-gray-800">
            <ArrowRightIcon />
          </button>
      </div>
    </div>
  </div>
</template>

<script setup>
  import CellCompleteResults from './CellCompleteResults.vue';
  import {
    defineEmits,
    defineProps,
    ref,
    onMounted,
    onBeforeMount,
    nextTick,
    inject,
    watch,
    computed
  } from 'vue';
  import Papa from 'papaparse';
  import axios from 'axios';
  import { Chart, registerables } from 'chart.js';
  import {
    ArrowLeftIcon,
    ArrowRightIcon,
    FunnelIcon,
    ChevronDownIcon,
    XMarkIcon,
    ArrowDownTrayIcon,
    ChatBubbleLeftIcon,
    PencilIcon,
    CheckIcon,
    PlayIcon
  } from '@heroicons/vue/24/outline';
  import { PauseIcon } from '@heroicons/vue/24/solid';
  import { DataFrame, Series } from 'danfojs';
  import CellTimeFilter from './CellTimeFilter.vue';
  import CellInfoHidden from './CellInfoHidden.vue';

  const API_BASE_URL = inject('API_BASE_URL');

  Chart.register(...registerables);

  const box_indexing_form = ref('1');
  const images_show = ref([false]);
  const dialog = ref(false);
  const csv_data = ref([]);
  const csv_headers = ref([]);
  const image_properties = ref([]);
  const canvases = ref([]);
  const show_info_box = ref(false);
  const tmp_data = ref([]);
  const box_index = ref('Not Selected');
  const pos_x = ref(0);
  const pos_y = ref(0);
  const selectMode = ref('');
  const show_visualizer = ref(false);
  const df = ref(null);
  const selected_value_filter = ref('');
  const show_manual_number = ref(false);
  const selected_from = ref(0);
  const selected_to = ref(0);
  const show_original = ref(true);
  const original_button_text = ref('SHOW ORIGINAL');
  const data_img_opacity = ref(100);
  const current_result_index = ref(0);
  const calculated_data = ref({});
  const current_box_row = ref(-1);
  const graph_type = ref('');
  const graph_param = ref('');
  const isCommentVisible = ref(false);
  const comments = ref({});
  const selected_result_id = ref(0);
  const current_total_scratch_width = ref('Not calculated yet');
  const information_text = ref('Here you can choose a result to inspect');
  const shiftLeft = ref(false);
  const complete_showing_index = ref(0);
  const filtered_results = ref([]);
  const excluder = ref({
    excluded: false,
    excluded_row: -1,
    indexes: [],
    counter: 0,
    imgRef: null
  });
  const props = defineProps({
    images: {
      type: Object,
      default: () => {}
    }
  });
  const no_displayed_results = ref(3);
  const showing_index = ref(0);

  const min_to_show = computed(() => {
    if (show_complete_only_results.value) {
      return Math.min(complete_showing_index.value + no_displayed_results.value, filtered_results.value.length);
    }
    return Math.min(no_displayed_results.value + showing_index.value, props.images.length);
  } 
  
  );

  const current_unit = ref('Px');
  const to_unit = ref('Px');
  const no_boxes = {
    rows: 12,
    cols: 10
  };
  const show_complete_only_results = ref(false);
  const show_scratch_info = ref({
    show: false,
    width: -1,
    graph: false,
    playing: false,
    info: false
  });
  const scratchTop = ref('0px');
  const show_approximated_width = ref(false);
  const scratchEl = ref(null);
  const columns = ref([
    'avg_top_cells_vec_x',
    'avg_top_cells_vec_y',
    'top_cells',
    'avg_vec_x',
    'avg_vec_y',
    'count',
    'avg_normal_vec_x',
    'avg_normal_vec_y',
    'angle',
    'box_boundary_distance'
  ]);
  const new_comment = ref({});
  const stats = ref({});


  watch(to_unit, (newValue) => {
    if (newValue !== current_unit.value) {
          current_total_scratch_width.value = convert_to_area_unit(current_total_scratch_width.value);
          current_unit.value = newValue;
    }
  });

  watch(showing_index, (newValue) => {
      if(newValue >= props.images.length) {
          showing_index.value = 0;
      }
      if(newValue < 0) {
          showing_index.value = props.images.length - 1;
      }
  });




  const handle_click_left = () => {
    if (show_complete_only_results.value) {
      complete_showing_index.value -= 1;
      if (complete_showing_index.value < 0) {
        complete_showing_index.value = filtered_results.value.length - 1;
      }
    } else {
      showing_index.value -= 1;
      if (showing_index.value < 0) {
        showing_index.value = props.images.length - 1;
      }
    }

  };


  const handle_click_right = () => {
    if (show_complete_only_results.value) {
      complete_showing_index.value += 1;
      if (complete_showing_index.value >= filtered_results.value.length) {
        complete_showing_index.value = 0;
      }
    } else {
      showing_index.value += 1;
      if (showing_index.value >= props.images.length) {
        showing_index.value = 0;
      }
    }
  };


  const handle_update_check = (val) => {
      filtered_results.value = props.images.filter((result) => {
          return result.fileNames.length > 0;
      });
      complete_showing_index.value = 0;
      show_complete_only_results.value = val;
  }


  const emit = defineEmits(['reload', 'filterTime']);


  const toggleComments = async (id) => {
    isCommentVisible.value = !isCommentVisible.value;
    selected_result_id.value = id;
    try {
      const res = await axios.get(`${API_BASE_URL}/resultComment?ID=${id}`);
      comments.value = res.data;
      console.log(comments.value);
    } catch (error) {
      console.log(error);
    }
  };

  const handleShift = () => {
    shiftLeft.value = !shiftLeft.value;
  };

  const base64ToBlob = (base64, mimeType) => {
    const base64Data = base64.split(',')[1] || base64;

    try {
      const byteCharacters = atob(base64Data); // Decode Base64
      const byteNumbers = new Array(byteCharacters.length);

      for (let i = 0; i < byteCharacters.length; i++) {
        byteNumbers[i] = byteCharacters.charCodeAt(i);
      }

      const byteArray = new Uint8Array(byteNumbers);
      return new Blob([byteArray], { type: mimeType });
    } catch (error) {
      console.error('Base64 decoding failed. Invalid Base64 string.', error);
      return null;
    }
  };

  const clean_excluder = () => {
    excluder.value.excluded_row = -1;
    excluder.value.indexes = [];
  }

  const handleExcludedClick = () => {
    excluder.value.excluded = !excluder.value.excluded;
    excluder.value.counter++;
    if (excluder.value.counter > 0 && excluder.value.counter % 2 == 0) {
      show_visualizer.value = true;
      init_visualizer(excluder.value.imgRef, excluder.value.excluded_row);
      clean_excluder();
    } 
  };

  const downloadImage = (files, fileNames) => {
    const link = document.createElement('a');
    link.href = URL.createObjectURL(base64ToBlob(files[current_result_index.value], 'image/png'));
    link.download = fileNames[current_result_index.value];
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const opacitySliderChange = () => {
    const img_place = document.querySelector('.original-image');
    if (img_place.querySelector('img')) {
      img_place.querySelector('img').style.opacity = data_img_opacity.value + '%';
      console.log(data_img_opacity.value);
    }
  };

  const hideIntervalSelector = (e) => {
    show_manual_number.value = false;
  };

  const hideCSV = () => {
    dialog.value = !dialog.value;
  };

  const showDataImage = async (id) => {
    try {
      show_original.value = !show_original.value;

      const img_place = document.querySelector('.original-image');
      if (!img_place) {
        console.error("No element with class '.original-image' found in the DOM.");
        return;
      }

      if (show_original.value) {
        original_button_text.value = 'SHOW ORIGINAL';
      } else {
        original_button_text.value = 'HIDE ORIGINAL';
      }

      const img_data_result = await getDataImage(id);
      if (img_data_result && img_data_result.length > 0) {
        let img = img_place.querySelector('img');
        if (!img) {
          img = document.createElement('img');
          img.style.width = '100%';
          img.style.height = '100%';
          img.style.position = 'relative';
          img.style.top = '-15vh';
          img.style.left = '-2vw';
          img.style.boxShadow = '0px 0px 1px white';
          img.style.opacity = '100%';
          img_place.appendChild(img);
        }

        img.setAttribute('src', img_data_result[0]);
      } else {
        console.warn('No image data found for the provided ID.');
      }
    } catch (error) {
      console.error('An error occurred while showing the data image:', error);
    }
  };


  const draw_scratch_width_line = (ctx,x,y,scratch_width) => {
      ctx.beginPath();
      ctx.fillStyle = "yellow";
      ctx.strokeStyle = "yellow";
      ctx.moveTo(x,y);
      ctx.lineTo(x + scratch_width,y);
      ctx.stroke();
      ctx.fill();
  }



  const getDataImage = async (id) => {
    try {
      const res = await axios.post(`${API_BASE_URL}/resultData`, { ID: id });
      return res.data;
    } catch (error) {
      console.error('Error fetching data image:', error);
      throw error;
    }
  };

  const handle_filtered_time = (date_filter) => {
    console.log('Filtered date: ', date_filter);
    emit('filterTime', date_filter);
  };

  const indexes = ref([]);
  const draw_selected_filter = (index) => {
    if (csv_data.value) {
        csv_data.value.forEach((val, index) => {
          if (
            val[selected_value_filter.value] >= selected_from.value &&
            val[selected_value_filter.value] <= selected_to.value
          ) {
            indexes.value.push(index);
          }
        });
        console.log(indexes.value);

        show_manual_number.value = !show_manual_number.value;
      } else {
        window.alert('CSV data wasnt loaded yet');
      }
  };

  const find_boxes = (e) => {
    const cell_canvas = document.querySelector('.cell-canvas');
    console.log(cell_canvas);
    if (cell_canvas == null) {
      window.alert('Cell canvas has not loaded yet');
    } else {
      show_manual_number.value = !show_manual_number.value;
    }
  };

  const removeResult = async (id) => {
    fetch(`${API_BASE_URL}/removeResult?id=${id}`, {
      method: 'DELETE'
    })
      .then((response) => {
        if (response.ok) {
          emit('reloadData');
        } else {
          alert('Failed to delete result.');
        }
      })
      .catch((error) => {
        console.error('Error deleting result:', error);
      });
  };

  const showResult = (index, len) => {
    if (len <= 0) {
      information_text.value = 'Result is not fully loaded yet';
      return;
    }
    images_show.value[index] = !images_show.value[index];
    const img_place = document.querySelector('.original-image');
    if (img_place.querySelector('img')) {
      img_place.querySelector('img').remove();
    }

    current_total_scratch_width.value = approximate_scratch_width();
    show_approximated_width.value = !show_approximated_width.value;
    current_result_index.value = 0;
  };
  const hideVisualizer = () => {
    show_visualizer.value = !show_visualizer.value;
  };
  const showCSV = async (csv_filename, id) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/downloadCSV/${csv_filename}/${id}`, {
        responseType: 'blob'
      });
      if(response.status >= 400) {
        console.error('Error downloading CSV file:', response.statusText);
        return;
      }
      const csvBlob = response.data;
      const csvText = await csvBlob.text();

      const parsedData = Papa.parse(csvText, { header: true });

      csv_headers.value = parsedData.meta.fields;
      csv_data.value = parsedData.data;
      dialog.value = !dialog.value;
    } catch(error) {
        console.log(error);
    }
  };


  const draw_initial_state = (ctx,imgElement,text) => {

    ctx.fillStyle = 'rgba(0,0,0,0.3)';
      ctx.drawImage(
        imgElement,
        0,
        0,
        imgElement.naturalWidth / 2,
        imgElement.naturalHeight / 2
      );
    ctx.fillRect(122, 88, 708, 530);
    ctx.fillStyle = 'white';
    ctx.font = '40px Arial';
    ctx.fillText(text, imgElement.naturalWidth / 9, imgElement.naturalHeight / 4);
  }


  const handleImageLoad = async (e, index, csvName, id) => {
    const imgElement = e.target;
    const imgWidth = imgElement.naturalWidth;
    const imgHeight = imgElement.naturalHeight;
    if (e.target.parentNode.querySelector('canvas')) {
      e.target.parentNode.querySelector('canvas').remove();
    }
    const canvas = document.createElement('canvas');
    canvas.classList.add('cell-canvas');
    canvas.width = imgWidth / 2;
    canvas.height = imgHeight / 2;
    const ctx = canvas.getContext('2d');

    ctx.drawImage(imgElement, 0, 0, imgWidth / 2, imgHeight / 2);

    canvases.value[index] = canvas;
    console.log(canvases.value[index]);
    e.target.parentNode.appendChild(canvas);
    draw_initial_state(ctx, imgElement,"Click or hover over the canvas");
    canvas.addEventListener('mousemove', (event) => handle_movement(event, imgElement));
    canvas.addEventListener('click', (event) => handle_click(event, imgElement));
    canvas.addEventListener('mouseenter', (event) => handle_mouseenter(event));
    await preload_csv(csvName, id);
  };

  const handle_mouseenter = (e) => {
      const ctx = e.target.getContext('2d');
      indexes.value.forEach((val) => {
        const num_boxes_y = Math.floor(val / no_boxes.cols);
        const num_boxes_x = val % no_boxes.cols; 
        ctx.fillStyle = 'rgba(173, 216, 230, 0.5)';
        ctx.strokeRect(num_boxes_x * 70 + 122, num_boxes_y * 43 + 88, 73, 45);
        ctx.fillRect(num_boxes_x * 70 + 122, num_boxes_y * 43 + 88, 73, 45);
      });
      indexes.value = [];
  };

  const getDPI = () => {
    const div = document.createElement('div');
    div.style.width = '1in'; // 1 inch
    div.style.visibility = 'hidden';
    document.body.appendChild(div);
    const dpi = div.offsetWidth;
    document.body.removeChild(div);
    return dpi;
  };

  const micron_per_px = 1.67;
const mm_per_px = micron_per_px / 1000;
const px2_to_mm2 = mm_per_px ** 2;

const convert_to_area_unit = (value) => {
  let area_mm2;

  // Nejdřív převedeme hodnotu do mm² podle current_unit
  switch (current_unit.value) {
    case 'Px':
      area_mm2 = value * px2_to_mm2;
      break;
    case 'Cm':
      area_mm2 = value * 100; // 1 cm² = 100 mm²
      break;
    case 'In':
      area_mm2 = value * (25.4 ** 2); // 1 inch² = 645.16 mm²
      break;
    case 'Mm':
      area_mm2 = value;
      break;
    default:
      throw new Error("Neznámá jednotka vstupu: " + current_unit.value);
  }

  // A teď převedeme z mm² do cílové jednotky
  let converted;
  switch (to_unit.value) {
    case 'Px':
      converted = area_mm2 / px2_to_mm2;
      break;
    case 'Cm':
      converted = area_mm2 / 100;
      break;
    case 'In':
      converted = area_mm2 / (25.4 ** 2);
      break;
    case 'Mm':
      converted = area_mm2;
      break;
    default:
      throw new Error("Neznámá jednotka výstupu: " + to_unit.value);
  }

  // Po převodu aktualizuj stav
  current_unit.value = to_unit.value;
  return converted;
};

const convert_from_px = (value) => {
  const px_to_mm = 1.67; // zde si nastavíš poměr, např. 1px = 1.67mm

  const mm = value * px_to_mm; // vždy nejdřív převedeme z px do mm

  let converted;
  switch (to_unit.value) {
    case 'Px':
      converted = value; // žádný převod
      break;
    case 'Mm':
      converted = mm;
      break;
    case 'Cm':
      converted = mm / 10;
      break;
    case 'In':
      converted = mm / 25.4;
      break;
    default:
      throw new Error("Neznámá jednotka výstupu: " + to_unit.value);
  }

  return  Math.round(converted);
};






  const approximate_scratch_width = () => {
    const REFERENCE_WIDTH = 713;
    const IMAGE_HEIGHT = 524;

    const getBoundaryDistance = (index) =>
      Number(csv_data.value[index]?.box_boundary_distance || 0);

    let first_box_top = convertIndex(0, 0, no_boxes.rows);
    let last_box_top = convertIndex(0, no_boxes.cols - 1, no_boxes.rows);
    let b =
      REFERENCE_WIDTH -
      getBoundaryDistance(first_box_top) / 2 -
      getBoundaryDistance(last_box_top) / 2;
    let first_box_bottom = convertIndex(no_boxes.rows - 1, 0, no_boxes.rows);
    let last_box_bottom = convertIndex(no_boxes.rows - 1, no_boxes.cols - 1, no_boxes.rows);
    let c =
      REFERENCE_WIDTH -
      getBoundaryDistance(first_box_bottom) / 2 -
      getBoundaryDistance(last_box_bottom) / 2;

    let total_width = ((b + c) / 2) * IMAGE_HEIGHT;

    return convert_to_area_unit(total_width);
  };

  const approximate_scratch_width_table = (table) => {
      const REFERENCE_WIDTH = 713;
      const IMAGE_HEIGHT = 524;

      const getBoundaryDistance = (index) =>
        Number(table[index]?.box_boundary_distance || 0);

      let first_box_top = convertIndex(0, 0, no_boxes.rows);
      let last_box_top = convertIndex(0, no_boxes.cols - 1, no_boxes.rows);
      let b =
        REFERENCE_WIDTH -
        getBoundaryDistance(first_box_top) / 2 -
        getBoundaryDistance(last_box_top) / 2;

      let first_box_bottom = convertIndex(no_boxes.rows - 1, 0, no_boxes.rows);
      let last_box_bottom = convertIndex(no_boxes.rows - 1, no_boxes.cols - 1, no_boxes.rows);
      let c =
        REFERENCE_WIDTH -
        getBoundaryDistance(first_box_bottom) / 2 -
        getBoundaryDistance(last_box_bottom) / 2;

      let total_width = ((b + c) / 2) * IMAGE_HEIGHT;

      return convert_to_area_unit(total_width);
};


  //1426x1048
  //713x524
  //244<-
  //256->

  const get_associated_data = (boxRow, column_name) => {
    const data = [];
    if (csv_data.value) {
      if(selectMode.value == '3') {
          const volume = boxRow[0] * boxRow[1];
          const indexes = Array.from({ length: volume }, (_, i) => i);
          indexes.forEach((val) => {
            const row = Math.floor(val / boxRow[1]); // aktuální řádek v mřížce
            const column = val % boxRow[1]; // aktuální sloupec v mřížce
            const index = convertIndex(row, column, no_boxes.rows);
            data.push(csv_data.value[index][column_name]);
          });
          return data;
      }
      csv_data.value.forEach((val, index) => {
        if (index >= boxRow && index < boxRow + 10) {
          console.log(val[column_name]);
          data.push(val[column_name]);
        }
      });
    }
    return data;
  };

  const decide_graph_labels = (r) => {
    const out_labels = [];
    if(selectMode.value === '2') {
        const real_labels = Array.from({ length: 10 }, (_, i) => convertIndex(i,r,no_boxes.rows));
        return real_labels;
    } 
    if (selectMode.value === '3') {
        const total = r[0] * r[1];
        const indexes = Array.from({ length: total }, (_, i) => i);

        const real_labels = indexes.map((val) => {
          const row = Math.floor(val / r[1]); // aktuální řádek v mřížce
          return convertIndex(val, row, no_boxes.rows);
        });

        return real_labels;
  }
  if (excluder.value.counter % 2 == 0) {
      const indexes_new = [...new Set(excluder.value.indexes)];
      const real_labels = Array.from({ length: 10 }, (_, i) => convertIndex(r,i,no_boxes.rows));
      console.log(indexes_new, real_labels);
      const new_labels = real_labels.filter((val) => !indexes_new.includes(val));
      return new_labels;
    } else {
      out_labels.push(...Array.from({ length: 10 }, (_, i) => convertIndex(r,i,no_boxes.rows)));
    }

    console.log(out_labels);
    return out_labels;
  };

  const init_visualizer = (imgEL, boxRow) => {
    const vis_elem = document.querySelector('.data-visualizer');
    const boundary_graph_place = vis_elem.querySelector('.boundary-distance-graph');
    console.log(boundary_graph_place);
    vis_elem.querySelectorAll('canvas').forEach((val) => {
      val.remove();
    });
    const new_canvas = document.createElement('canvas');
    new_canvas.width = 800;
    new_canvas.height = 150;

    boundary_graph_place.appendChild(new_canvas);

    let graph_labels = [];
    let boundary_values = [];

    if(selectMode.value === '3') {
      graph_labels = decide_graph_labels(boxRow);
      boundary_values = get_associated_data(boxRow, 'box_boundary_distance');
      current_box_row.value = boxRow[0];
    } else {
      graph_labels = decide_graph_labels(boxRow);
      boundary_values = get_associated_data(boxRow, 'box_boundary_distance');
      current_box_row.value = boxRow;
    }
    console.log(graph_labels);
    const converted_boundary = boundary_values.map((val) => {
      return Number.parseInt(val);
    });

    const boundary_chart = new Chart(new_canvas.getContext('2d'), {
      type: 'line',
      data: {
        labels: graph_labels,
        datasets: [
          {
            label: 'Sample Data',
            data: converted_boundary,
            borderColor: 'rgba(75, 192, 192, 1)',
            backgroundColor: 'rgba(75, 192, 192, 0.2)',
            fill: true,
            borderWidth: 3
          }
        ]
      },
      options: {
        responsive: true,
        scales: {
          x: {
            title: {
              display: true,
              text: 'Box Numbers'
            }
          },
          y: {
            title: {
              display: true,
              text: 'Values'
            },
            beginAtZero: true
          }
        }
      }
    });
    console.log(boundary_chart);
  };

  let result_interval_id = null;
  const animate_results = (index) => {
  show_scratch_info.value.playing = !show_scratch_info.value.playing;
  
  if (!show_scratch_info.value.playing) {
    stop_animation();
    return;
  }

  result_interval_id = setInterval(() => {
    current_result_index.value++;

    if (show_complete_only_results.value) {
      if (current_result_index.value >= filtered_results.value[index]?.files?.length) {
        current_result_index.value = 0;
      }
    } else {
      if (current_result_index.value >= props.images[index]?.files?.length) {
        current_result_index.value = 0;
      }
    }

    const img_el = document.querySelector(`.image${index}`);

    if (index < 0 || index >= filtered_results.value.length || index >= props.images.length) {
      console.log("Invalid index:", index);
      return;
    }

    let src = null;

    if (show_complete_only_results.value) {
      if (filtered_results.value[index] && filtered_results.value[index].files && filtered_results.value[index].files.length > 0) {
        src = filtered_results.value[index].files[current_result_index.value];
      } else {
        console.log("No files in filtered_results for index", index);
      }
    } else {
      if (props.images[index] && props.images[index].files && props.images[index].files.length > 0) {
        src = props.images[index].files[current_result_index.value];
      } else {
        console.log("No files in props.images for index", index);
      }
    }
    if (src) {
      img_el.setAttribute('src', src);
    } else {
      console.log('Image source is undefined or empty');
    }
    
  }, 500);
};


  const stop_animation = () => {
    show_scratch_info.value.playing = false;
    clearInterval(result_interval_id);
  };

  const change_graph = () => {
    const vis_elem = document.querySelector('.data-visualizer');
    const boundary_graph_place = vis_elem.querySelector('.boundary-distance-graph');
    console.log(boundary_graph_place);
    vis_elem.querySelectorAll('canvas').forEach((val) => {
      val.remove();
    });
    const new_canvas = document.createElement('canvas');
    new_canvas.width = 800;
    new_canvas.height = 150;

    boundary_graph_place.appendChild(new_canvas);

    const graph_labels = decide_graph_labels(current_box_row.value);
    console.log(graph_labels);
    if (graph_type.value != '' && graph_param.value != '') {
      const boundary_values = get_associated_data(current_box_row.value, graph_param.value);
      const converted_boundary = boundary_values.map((val) => {
        return Number.parseInt(val);
      });
      const boundary_chart = new Chart(new_canvas.getContext('2d'), {
        type: graph_type.value,
        data: {
          labels: graph_labels,
          datasets: [
            {
              label: 'Sample Data',
              data: converted_boundary,
              borderColor: 'rgba(75, 192, 192, 1)',
              backgroundColor: 'rgba(75, 192, 192, 0.2)',
              fill: true,
              borderWidth: 3
            }
          ]
        },
        options: {
          responsive: true,
          scales: {
            x: {
              title: {
                display: true,
                text: 'Box Numbers'
              }
            },
            y: {
              title: {
                display: true,
                text: 'Values'
              },
              beginAtZero: true
            }
          }
        }
      });
      console.log(boundary_chart);
    }
  };

  const handle_click = (e, imgElement) => {
    const ctx = e.target.getContext('2d');
    const rect = e.target.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    ctx.strokeStyle = '#222222';
    ctx.lineWidth = 3;
    const newImage = imgElement.cloneNode(true);
    newImage.style.width = '50%';
    newImage.style.height = '50%';
    if (x >= 122 && x <= 830 && y >= 88 && y <= 614) {
      const new_x = x - 122;
      const new_y = y - 88;
      const num_boxes_x = Math.floor(new_x / 70);
      const num_boxes_y = Math.floor(new_y / 43);
      console.log(num_boxes_x, num_boxes_y);
      let box_num = num_boxes_y * 10 + num_boxes_x;
      box_num = convertIndex(num_boxes_y, num_boxes_x, 12);
      switch (selectMode.value) {
        case '4':
        case '':
          if (csv_data.value) {
            csv_data.value.forEach((val, index) => {
              if (index == box_num) {
                tmp_data.value = [];
                show_info_box.value = !show_info_box.value;
                tmp_data.value.push(val);
                console.log(val);
              }
            });
          }
          break;
        case '1':
          if (num_boxes_y === excluder.value.excluded_row && excluder.value.excluded) {
            ctx.fillStyle = 'rgba(255,0,0,0.4)';
            ctx.fillRect(num_boxes_x * 70 + 122, num_boxes_y * 43 + 88, 73, 45);
            excluder.value.indexes.push(box_num);
            excluder.value.imgRef = imgElement;
          } else {
            show_visualizer.value = true;
            init_visualizer(imgElement, num_boxes_y);
          }
          break;
        case '2':
          if (num_boxes_y === excluder.value.excluded_row && excluder.value.excluded) {
            ctx.fillStyle = 'rgba(255,0,0,0.4)';
            ctx.fillRect(num_boxes_x * 70 + 122, num_boxes_y * 43 + 88, 73, 45);
            excluder.value.indexes.push(box_num);
            excluder.value.imgRef = imgElement;
          } else {
            show_visualizer.value = true;
            init_visualizer(imgElement, num_boxes_x);
          }
          break;
        case '3':
          show_visualizer.value = true;
          init_visualizer(imgElement, [num_boxes_y, num_boxes_x]);
          
          break;
      }
      console.log(box_num);
    }
  };

  const draw_boundary_line = (ctx, x, y, width, height, distance) => {
    ctx.beginPath();
    ctx.moveTo(x + width / 2, y + height / 2);
    ctx.lineTo(x + width / 2 + distance, y + height / 2);
    ctx.fillStyle = "red";
    ctx.font = "900 24px Arial";
    ctx.fillText(`${Math.abs(distance)}px`,x,y+50,210);
    ctx.strokeStyle = 'red';
    ctx.lineWidth = 5;
    ctx.stroke();
  };

  const preload_csv = async (csvName, id) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/downloadCSV/${csvName}/${id}`, {
      responseType: 'blob'
    });
    if(response.status >= 400) {
      console.error('Error downloading CSV file:', response.statusText);
      return;
    }
    const csvBlob = response.data;
    const csvText = await csvBlob.text();

    const parsedData = Papa.parse(csvText, { header: true });

    csv_headers.value = parsedData.meta.fields;
    csv_data.value = parsedData.data;

    // Normalize Data
    const normalizedData = parsedData.data.map((row) => {
      let normalizedRow = {};
      csv_headers.value.forEach((header) => {
        normalizedRow[header] = row[header] || null;
      });
      return normalizedRow;
    });

    df.value = new DataFrame(normalizedData);

    calc_data(df.value);
    console.log(calculated_data.value);

    } catch(error) {
      console.error('Error preloading CSV:', error);
    }
   
  };

  const load_table = async (name, id, csv_table) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/downloadCSV/${name}/${id}`, {
        responseType: 'blob'
      });
      if(response.status >= 400) {
        console.error('Error downloading CSV file:', response.statusText);
        return;
      }
      const csvText = await response.data.text();
      const parsedData = Papa.parse(csvText, { header: true });
      const full_csv = {
        headers: parsedData.meta.fields,
        data: parsedData.data
      };
      csv_table.push(full_csv);
    } catch (error) {
      console.error('Could not load table');
    }
  };

  const show_scratch_width_graph = (scratch_widths) => {
    const place = document.querySelector('.scratch-width-visualisation');
    let canvas = place.querySelector('canvas');
    if (canvas) {
      canvas.remove();
    }
    canvas = document.createElement('canvas');
    place.appendChild(canvas);
    const ctx = canvas.getContext('2d');
    const labels = Array.from({ length: scratch_widths.length }, (_, i) => i);
    const boundary_values = scratch_widths;
    const converted_boundary = boundary_values.map((val) => {
      return Number.parseInt(val);
    });
    const boundary_chart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [
          {
            label: 'Scratch width',
            data: converted_boundary,
            borderColor: 'rgba(75, 192, 192, 1)',
            backgroundColor: 'rgba(75, 192, 192, 0.2)',
            fill: true,
            borderWidth: 3
          }
        ]
      },
      options: {
        responsive: true,
        scales: {
          x: {
            title: {
              display: true,
              text: 'Result indexes'
            }
          },
          y: {
            title: {
              display: true,
              text: `Scratch area in ${current_unit.value} `
            },
            beginAtZero: true
          }
        }
      }
    });
    console.log(boundary_chart);
  };

  const precalculate_all_scratch_widths = async (res) => {
    if (show_scratch_info.value.graph) {
      return;
    }
    const csv_tables = [];
    for (const name of res.csvNames) {
      await load_table(name, res.id, csv_tables);
    }
    const scratch_widths = [];
    for (const table of csv_tables) {
      scratch_widths.push(approximate_scratch_width_table(table.data));
    }
    console.log(scratch_widths);
    show_scratch_info.value.graph = true;
    show_scratch_width_graph(scratch_widths);
  };

  const calc_data = (data_frame) => {
    const stats = {};

    columns.value.forEach((col) => {
      const numericValues = data_frame[col].values.map((value) => parseFloat(value));

      const validData = numericValues.filter((value) => !isNaN(value));

      if (validData.length === 0) {
        stats[col] = { mean: null, median: null, std: null };
      } else {
        stats[col] = {
          mean: (validData.reduce((sum, value) => sum + value, 0) / validData.length).toFixed(2),
          median: calculateMedian(validData).toFixed(2),
          std: calculateStd(validData).toFixed(2)
        };
      }
    });

    calculated_data.value = stats;
    console.log(calculated_data);
  };

  function calculateMedian(values) {
    const sorted = values.sort((a, b) => a - b);
    const mid = Math.floor(sorted.length / 2);
    return sorted.length % 2 === 0 ? (sorted[mid - 1] + sorted[mid]) / 2 : sorted[mid];
  }

  function calculateStd(values) {
    const mean = values.reduce((sum, value) => sum + value, 0) / values.length;
    const variance =
      values.reduce((sum, value) => sum + Math.pow(value - mean, 2), 0) / values.length;
    return Math.sqrt(variance);
  }

  function convertIndex(current_row, current_col, rows) {
    let new_index = current_col * rows + current_row;
    return new_index;
  }

  //65 x 45
  const handle_movement = (e, imgElement) => {
    const ctx = e.target.getContext('2d');
    const rect = e.target.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    ctx.strokeStyle = '#222222';
    ctx.lineWidth = 3;
    if (x >= 122 && x <= 830 && y >= 88 && y <= 614) {
      ctx.clearRect(0, 0, imgElement.naturalWidth, imgElement.naturalHeight);
      ctx.drawImage(imgElement, 0, 0, imgElement.naturalWidth / 2, imgElement.naturalHeight / 2);
      const new_x = x - 122;
      const new_y = y - 88;
      const num_boxes_x = Math.floor(new_x / 70);
      const num_boxes_y = Math.floor(new_y / 43);
      switch (selectMode.value) {
        case '':
          ctx.strokeRect(num_boxes_x * 70 + 122, num_boxes_y * 43 + 88, 73, 45);
          break;
        case '5':
          ctx.strokeRect(num_boxes_x * 70 + 122, num_boxes_y * 43 + 88, 73, 45);
          break;
        case '1': {
          ctx.fillStyle = 'rgba(173, 216, 230, 0.5)';

          ctx.strokeRect(126, num_boxes_y * 43 + 88, 699, 45);
          if (excluder.value.excluded) {
            excluder.value.excluded_row = num_boxes_y;
            for (let i = 0; i < no_boxes.cols; i++) {
              let v = num_boxes_y * 10 + i;
              v = convertIndex(num_boxes_y, i, no_boxes.rows);
              if (excluder.value.indexes.includes(v)) {
                ctx.fillStyle = 'rgba(255,0, 0, 0.8)';
                ctx.fillRect(70 * i + 122, num_boxes_y * 43 + 88, 73, 45);
              } else {
                ctx.fillStyle = 'rgba(173, 216, 230, 0.5)';
                ctx.fillRect(70 * i + 122, num_boxes_y * 43 + 88, 73, 45);
              }
            }
          } else {
            if(excluder.value.excluded_row !== -1) {
              break;
            }
            ctx.fillRect(126, num_boxes_y * 43 + 88, 699, 45);
            show_scratch_info.value.show = true;
          const info_y = e.target.getBoundingClientRect().top + 88 + (num_boxes_y - 2) * 43;
          scratchTop.value = info_y + 'px';
          console.log(scratchTop.value, e.target.clientY);
          let first_index = convertIndex(num_boxes_y, 0, 12);
          let last_index = convertIndex(num_boxes_y, 9, 12);
          console.log('Indexes: ', [first_index, last_index]);
          console.log(
            'FIRST BOX DISTANCE: ',
            Number(csv_data.value[first_index].box_boundary_distance)
          );
          console.log(
            'LAST BOX DISTANCE: ',
            Number(csv_data.value[last_index].box_boundary_distance)
          );
          const scratch_width =
            Math.floor(713 -
            Number(csv_data.value[first_index].box_boundary_distance * 0.5) -
            Number(csv_data.value[last_index].box_boundary_distance * 0.5));
          show_scratch_info.value.width = convert_from_px(scratch_width);
          // 713 = len1 + s_w + len2
          const calculated_scratch_x =  (0 * 70 + 122) + Number(csv_data.value[first_index].box_boundary_distance * 0.5);
          draw_scratch_width_line(ctx,calculated_scratch_x,num_boxes_y * 43 + 88 + 22,scratch_width);
          draw_boundary_line(
            ctx,
            0 * 70 + 122,
            num_boxes_y * 43 + 88,
            73,
            45,
            Number(csv_data.value[first_index].box_boundary_distance * 0.5)
          );
          draw_boundary_line(
            ctx,
            (no_boxes.cols - 1) * 70 + 122,
            num_boxes_y * 43 + 88,
            73,
            45,
            -Number(csv_data.value[last_index].box_boundary_distance * 0.5)
          );
          }
          break;
        }
        case '2':
          ctx.fillStyle = 'rgba(173, 216, 230, 0.5)';
          ctx.strokeRect(num_boxes_x * 70 + 122, 87, 73, 530);
          ctx.fillRect(num_boxes_x * 70 + 122, 87, 73, 530);

          break;
        case '3':
          ctx.fillStyle = 'rgba(173, 216, 230, 0.5)';
          ctx.strokeRect(122, 88, num_boxes_x * 70, num_boxes_y * 43);
          ctx.fillRect(122, 88, num_boxes_x * 70, num_boxes_y * 43);
          break;
        case '4':
          ctx.fillStyle = 'rgba(173, 216, 230, 0.5)';
          ctx.strokeRect(num_boxes_x * 70 + 122, num_boxes_y * 43 + 88, 73, 45);
          ctx.fillRect(num_boxes_x * 70 + 122, num_boxes_y * 43 + 88, 73, 45);
          break;
      }
      box_index.value = num_boxes_y * 10 + num_boxes_x;
      pos_x.value = Math.floor(new_x);
      pos_y.value = Math.floor(new_y);
    } else {
      show_scratch_info.value.show = false;
      draw_initial_state(ctx, imgElement,"Select specific box or area");
    }
  };

  const switch_result = (files, side, index, csvNames, id) => {
    if (side) {
      current_result_index.value = ++current_result_index.value % files.length;
    } else {
      current_result_index.value--;
      if (current_result_index.value <= 1) {
        current_result_index.value = files.length - 1;
      }
      if (current_result_index.value == files.length - 1) {
        current_result_index.value = 0;
      }
    }

    const img = document.querySelector(`.image${index}`);
    console.log('IMAGE: ', img);
    img.setAttribute('src', files[current_result_index.value]);

    /*const canvas = canvases.value[index];
        console.log("CANVAS",canvas);
        const imgWidth = img.naturalWidth;
        const imgHeight = img.naturalHeight;
        const ctx = canvas.getContext('2d');

        ctx.clearRect(0,0,img.naturalWidth,img.naturalHeight);
        ctx.drawImage(img, 0, 0, imgWidth / 2, imgHeight / 2);*/
  };

  const submitComment = async () => {
    try {
      const response = await axios.get(
        `${API_BASE_URL}/addComment?ID=${selected_result_id.value}&author=${new_comment.value.author}&text=${new_comment.value.text}`
      );
      toggleComments(selected_result_id.value);
    } catch (error) {
      console.log(error);
    }
  };
</script>

<style scoped>
  @import '../styles/CellResults.css';
</style>

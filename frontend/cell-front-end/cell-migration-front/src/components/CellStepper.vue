<template>
 <div class="h-100 max-w-4xl mx-auto p-10">
    <!-- Stepper -->
    <div class="relative h-95">
      <div class="absolute left-5 top-8 w-1 bg-gray-300 h-full"></div>

      <template v-for="(s, index) in steps" :key="s.id">
        <div class="flex items-start mb-8 relative">
          <div
            class="w-12 h-12 flex items-center justify-center rounded-full border-2 z-10"
            :class="{
              'bg-blue-500 text-white border-blue-500': step >= index,
              'border-gray-400 text-gray-500': step <= index + 1
            }"
          >
            {{ index + 1 }}
          </div>
          <div class="ml-8">
            <h3 class="text-xl font-semibold">{{ s.title }}</h3>
            <p class="text-gray-600">{{ s.description }}</p>
          </div>
        </div>
      </template>
    </div>

    <!-- Obsah kroku -->
    <div class="p-8 bg-white shadow-lg rounded-lg mt-8">
      <h2 class="text-2xl font-semibold">{{ steps[step].title }}</h2>
      <p class="text-gray-600 mt-4">{{ steps[step].description }}</p>

      <!-- Navigační tlačítka -->
      <div class="flex justify-between mt-8 items-center">
        <div v-if="step > 0" class="flex items-center space-x-4">
          <input
            type="number"
            v-model="stepsBack"
            min="1"
            :max="step"
            class="w-16 p-2 border rounded-md"
          />
          <button
            class="px-6 py-3 bg-gray-300 text-gray-700 rounded-lg"
            @click="prevStep(stepsBack)"
          >
            Back
          </button>
        </div>
        <button
          class="px-6 py-3 bg-blue-500 text-white rounded-lg"
          @click="nextStep"
        >
          {{ step === steps.length - 1 ? "Finish" : "Next" }}
        </button>
      </div>
    </div>
  </div>
    <!--
      <div v-show="menu_show == 2" class="bg-white p-6 rounded-lg shadow-md">
        <h2 class="text-lg font-semibold">{{ t('upload_segmented_data') }}</h2>
        <button @click="show_user_cell_picking" class="w-full bg-blue-400 text-white font-bold py-2 px-4 rounded hover:bg-blue-600 transition">
          {{ t('choose_cells') }}
        </button>
        <div class="mt-4 space-y-4">
          <div>
            <label class="block font-medium">{{ t('add_spots_csv') }}</label>
            <input @change="getSpots" type="file" class="block w-full p-2 border rounded-md" />
          </div>
          <div>
            <label class="block font-medium">{{ t('add_tracks_csv') }}</label>
            <input @change="getTracks" type="file" class="block w-full p-2 border rounded-md" />
          </div>
          <div>
            <label class="block font-medium">{{ t('num_brightfields') }}</label>
            <input v-model="segment_parameters.no_iter" min="0" max="70" type="number" class="block w-full p-2 border rounded-md" />
          </div>
          <div>
            <label class="block font-medium">{{ t('step_size') }}: {{ segment_parameters.step_size }}</label>
            <input v-model="segment_parameters.step_size" type="range" class="w-full" />
          </div>
        </div>
        <button @click="startSegmentation" class="mt-4 w-full bg-green-500 text-white py-2 rounded hover:bg-green-600 transition">
          {{ t('upload_data') }}
        </button>
      </div>
      
      <div v-show="menu_show == 0" class="bg-white p-6 rounded-lg shadow-md">
        <h2 class="text-lg font-semibold">{{ t('upload_brightfield') }}</h2>
        <input class="block w-full p-2 border rounded-md" accept=".tif" type="file" @change="pickFile" multiple />
        <div class="mt-4">
          <span class="block font-medium">{{ t('num_iterations') }}: {{ value }}</span>
          <input type="range" v-model="value" min="0" max="500" step="1" class="w-full" />
        </div>
        <button class="mt-4 w-full bg-blue-500 text-white py-2 rounded hover:bg-blue-600 transition" type="submit">
          {{ t('upload') }}
        </button>
      </div>
    -->
  </template>
  
  <script setup>
  import { ref,defineEmits} from "vue";
  const step = ref(0);
  const stepsBack = ref(0);
  const steps = [
    { id: 0, title: "Where to generate your result", description: "Choose a path where should be your result generated" },
    { id: 1, title: "Upload DAPI images", description: "In order to create graphical visualization you have to upload DAPI images" },
    { id: 2, title: "Upload BrightField images and generate levelsets", description: "Step to execute level set algoritms" },
    { id: 3, title: "Final segmentation of the result", description: "Finish by creating a segmented result" },

  ];
  

  const emit = defineEmits(['increase']);

  function nextStep() {
    if (step.value < steps.length) {
      step.value++;
      emit('increase',step.value);
    } 
  }
  
  function prevStep() {
    if (step.value >= 1) {
      if(stepsBack.value != 0) {
          step.value = Math.max(0, step.value - stepsBack.value);
      } else {
          step.value--;
      }
      emit('increase',step.value);
    }
  }
  </script>
  
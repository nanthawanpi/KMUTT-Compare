<script setup>
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  modelValue: String, // รับค่าการค้นหาจาก v-model
  placeholder: {
    type: String,
    default: 'ค้นหาหอพัก...', // ค่า default ถ้าไม่มีการกำหนดค่า placeholder
  },
});

const emit = defineEmits(['update:modelValue']);
</script>

<template>
  <div class="flex items-center space-x-2 w-full border border-gray-300 rounded-md p-2 shadow-sm h-10">
    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-500" viewBox="0 0 20 20" fill="currentColor">
      <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.98 3.742l3.495 3.496a1 1 0 01-1.414 1.414l-3.496-3.495A6 6 0 012 8z" clip-rule="evenodd" />
    </svg>
    <input
      type="text"
      :value="modelValue"
      @input="emit('update:modelValue', $event.target.value)"
      :placeholder="placeholder" 
      class="w-full border-none focus:outline-none focus:ring-0 focus:border-none bg-transparent"
    />
  </div>
</template>

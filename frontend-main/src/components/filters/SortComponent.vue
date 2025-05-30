<template>
    <div class="max-w-sm mx-auto">
      <select v-model="sortBy" @change="handleSortChange"
              class="cursor-pointer bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg">
        <option value="name">เรียงตามชื่อ</option>
        <option value="min_price">ราคาต่ำสุด</option>
        <option value="max_price">ราคาสูงสุด</option>
        <option value="distance">ระยะทาง</option>
      </select>
    </div>
  </template>
  
  <script setup>
  import { ref, watch } from 'vue';
  
  // รับ props จาก parent component
  const props = defineProps({
    dormitories: {
      type: Array,
      required: true,
    },
  });
  
  const sortBy = ref('name'); // ค่าเริ่มต้น
  
  // ฟังก์ชันจัดเรียงข้อมูล
  const sortDormitories = (sortType) => {
  if (sortType === 'min_price') {
    props.dormitories.sort((a, b) => a.min_price - b.min_price); // ราคาต่ำสุด -> สูงสุด
  } else if (sortType === 'max_price') {
    props.dormitories.sort((a, b) => b.max_price - a.max_price); // ราคาสูงสุด -> ต่ำสุด
  } else if (sortType === 'distance') {
    props.dormitories.sort((a, b) => a.distance - b.distance); // ใกล้ -> ไกล
  } else if (sortType === 'name') {
    props.dormitories.sort((a, b) => a.dormName.localeCompare(b.dormName)); // A-Z
  }
};

  
  // ฟังก์ชันเมื่อมีการเปลี่ยนแปลงการเลือก
  const handleSortChange = () => {
    sortDormitories(sortBy.value);
  };
  
  // การเฝ้าติดตามการเปลี่ยนแปลงของ sortBy
  watch(sortBy, (newSortType) => {
    sortDormitories(newSortType);
  });
  </script>
  
  <style scoped>
  /* ใส่สไตล์ที่ต้องการสำหรับ select */
  
  </style>
  
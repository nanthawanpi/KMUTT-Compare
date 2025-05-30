<script setup>
import { ref, watchEffect } from "vue";
import { useCompareStore } from "@/stores/compareStore";
import { storeToRefs } from 'pinia';
import router from '@/router';

const isExpanded = ref(false);
const compareStore = useCompareStore();
const { compareItems } = storeToRefs(compareStore);

// ฟังก์ชันลบหอพักออกจาก compareItems
const removeDormFromCompare = compareStore.removeDormFromCompare

// ฟังก์ชันสำหรับไปยังหน้าการเปรียบเทียบ
const goToComparePage = () => {
  if (compareItems.value.length === 2) {
    router.push({name: 'compare'}); // ไปยังหน้าการเปรียบเทียบ
  } else {
    alert('Please add exactly two dorms to compare.');
  }
};

// เพิ่มการตรวจสอบเมื่อ compareItems.length >= 2 ให้ expand อัตโนมัติ
watchEffect(() => {
  if (compareItems.value.length > 1) {
    isExpanded.value = true;
  }
});
</script>

<template>
  <div class="fixed">
    <!-- ปุ่มเปรียบเทียบ -->
    <button
      class="btn bg-blue-500 text-white px-6 py-3 rounded-lg shadow-lg transform transition-all hover:scale-105 flex items-center gap-2"
      @click="isExpanded = !isExpanded"
    >
      VS | รายชื่อหอพักที่ถูกเลือก
      <span :class="isExpanded ? 'rotate-180' : ''" class="transition-transform">
        <img src="../icons/up-arrow.png" class="w-4">
      </span>
    </button>

    <!-- รายชื่อหอพักที่ถูกเลือก -->
    <div
      v-if="isExpanded"
      class="bg-white shadow-lg mt-2 p-4 rounded-lg w-74 max-h-60 overflow-auto"
    >
      <p v-if="compareItems.length === 0" class="text-gray-500">
        ยังไม่มีหอพักที่ถูกเลือก
      </p>
      <ul v-else class="list-disc list-inside space-y-2">
        <li
          v-for="(dorm, index) in compareItems"
          :key="index"
          class="flex justify-between items-center border p-2"
        >
          <span class="text-black">{{ dorm.dormName }}</span>
          <button
            class="text-red-500 hover:text-red-700 text-lg"
            @click="removeDormFromCompare(index)"
          >
            ❌
          </button>
        </li>
      </ul>
      <div v-if="compareItems.length > 1">
        <button @click="goToComparePage" class="compare mt-3">
          เปรียบเทียบ
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fixed {
  position: fixed;
  bottom: 4rem;
  right: 4rem;
}

/* ปุ่ม */
.btn {
  background: linear-gradient(to right, #eb6d1e, #3500c7);
  color: white;
  padding: 12px 24px;
  font-size: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}


/* Hover */
.btn:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.3);
  background: linear-gradient(to right, #b35318, #260486);
}

/* ปุ่ม */
.compare {
  background: linear-gradient(to right, #95009a, #d44000);
  color: white;
  padding: 8px 10px;
  font-size: 16px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}


/* Hover */
.compare:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.3);
  background: linear-gradient(to right, rgb(212, 82, 1), #910074);
}

/* กล่องรายการหอพัก */
.bg-white {
  border: 1px solid #ddd;
}
</style>

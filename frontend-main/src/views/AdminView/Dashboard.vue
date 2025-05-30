<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Pie } from 'vue-chartjs'
import { Chart as ChartJS, Title, Tooltip, Legend, ArcElement, CategoryScale } from 'chart.js'
import { getDashboardStats } from '@/composables/getDashboardStats'
import ChartDataLabels from 'chartjs-plugin-datalabels'
import EditDormIcon from '@/components/icons/Dashboard/EditDormIcon.vue'
import EditUserIcon from '@/components/icons/Dashboard/EditUserIcon.vue'

ChartJS.register(ChartDataLabels, Title, Tooltip, Legend, ArcElement, CategoryScale)

// ใช้ API Root
const API_ROOT = import.meta.env.VITE_API_ROOT

// ดึงข้อมูลจาก Composable
const { totalDorms, totalUsers, activeUsers, offlineUsers, isLoading } = getDashboardStats(API_ROOT)

const router = useRouter()

// 🟢 สร้าง `chartData` และ `chartOptions`
const chartData = ref({
  labels: ['Active Users', 'Offline Users'],
  datasets: [
    {
      label: 'จำนวน',
      data: [0, 0], // 🛑 เริ่มต้นเป็น [0, 0] รอข้อมูลจาก API
      backgroundColor: ['#38c172', '#C8C8C8'],
      hoverOffset: 4
    }
  ]
})

const chartOptions = ref({
  responsive: true,
  plugins: {
    title: {
      display: true,
      text: 'สถิติระบบ'
    },
    legend: {
      position: 'top',
    },
    datalabels: {
      formatter: (value, ctx) => {
        let sum = ctx.chart.data.datasets[0].data.reduce((a, b) => a + b, 0);
        let percentage = ((value / sum) * 100).toFixed(2) + "%";
        return percentage;
      },
      color: '#fff',
      font: {
        weight: 'bold'
      }
    }
  }
})

// 🟢 `watch` ค่า `activeUsers` และ `offlineUsers` เพื่ออัปเดต Pie Chart
watch([activeUsers, offlineUsers], ([newActive, newOffline]) => {
  chartData.value.datasets[0].data = [newActive, newOffline]
})

const goToDormManagement = () => {
  router.push('/admin/dormitory-management')
}

const goToUserManagement = () => {
  router.push('/admin/user-management')
}
</script>



<template>
      <!-- 🏡 แสดงกราฟ -->
    <div class="bg-white p-6">
      <!-- Check if data exists before rendering chart -->
      <div v-if="!isLoading" class="h-96 max-w-4xl mx-auto flex flex-col items-center justify-center">
        <Pie :data="chartData" :options="chartOptions" />
      </div>
    </div>

  <div class="py-5 px-72">
    <!-- 🏡 การ์ดสถิติระบบ -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
  <!-- 🏡 หอพักทั้งหมด -->
  <div class="relative group">
    <div class="bg-blue-500 text-white p-4 rounded-lg shadow-md text-center w-full">
      <h2 class="text-xl font-semibold">หอพักทั้งหมด</h2>
      <p class="text-3xl font-bold">{{ totalDorms }}</p>
    </div>
    <div class="tooltip">หอพักทั้งหมดในระบบ</div>
  </div>

  <!-- 👤 Active Users -->
  <div class="relative group">
    <div class="bg-green-500 text-white p-4 rounded-lg shadow-md text-center w-full">
      <h2 class="text-xl font-semibold">Active Users</h2>
      <p class="text-3xl font-bold">{{ activeUsers }}</p>
    </div>
    <div class="tooltip">จำนวนผู้ใช้ที่เข้าสู่ระบบในขณะนี้</div>
  </div>

  <!-- 👥 ผู้ใช้ทั้งหมด -->
  <div class="relative group">
    <div class="bg-yellow-500 text-white p-4 rounded-lg shadow-md text-center w-full">
      <h2 class="text-xl font-semibold">ผู้ใช้ทั้งหมด</h2>
      <p class="text-3xl font-bold">{{ totalUsers }}</p>
    </div>
    <div class="tooltip">รวมถึงผู้ใช้ที่อาจไม่ได้ออนไลน์</div>
  </div>
</div>


<!-- การ์ดจัดการหอพัก และ การ์ดจัดการผู้ใช้ -->
<div class="flex flex-row items-center justify-between h-52 space-x-4 hover:cursor-pointer">
  <!-- 🏡 การ์ดจัดการหอพัก -->
  <div @click="goToDormManagement" class="w-1/2 h-full bg-white p-6 rounded-lg shadow-md flex justify-between items-center bg-dorm-management">
    <div class="w-full h-full flex flex-col items-start justify-center">
      <h2 class="text-2xl font-semibold mb-3 text-white">จัดการหอพัก</h2>
      <p class="text-white">ดูและจัดการหอพักทั้งหมด</p>
    </div>
    <div class="w-full h-full flex flex-col items-end justify-center">
      <EditDormIcon/>
    </div>
  </div>

  <!-- 👤 การ์ดจัดการผู้ใช้ -->
  <div @click="goToUserManagement" class="w-1/2 h-full bg-white p-6 rounded-lg shadow-md flex justify-between items-center bg-user-management">
    <div class="w-full h-full flex flex-col items-start justify-center">
      <h2 class="text-2xl font-semibold mb-3 text-white">จัดการผู้ใช้</h2>
      <p class="text-white">ดูและจัดการผู้ใช้ในระบบ</p>
    </div>
    <div class="w-full h-full flex flex-col items-end justify-center">
      <EditUserIcon/>
    </div>
  </div>
</div>


  </div>
</template>

<style scoped>
/* เพิ่มสไตล์ hover ให้ปุ่ม */
.bg-blue-500:hover, .bg-green-500:hover, .bg-yellow-500:hover, .bg-red-500:hover {
  transform: scale(1.05);
  transition: transform 0.3s ease;
}

.bg-dorm-management:hover,.bg-user-management:hover {
  transform: scale(1.02);
  transition: transform 0.3s ease;
}

.bg-dorm-management {
  background-image: linear-gradient(to right, #e65100, #f57c00, #fb8c00);
}
.bg-user-management {
  background-image: linear-gradient(to right, #1565c0, #0d47a1, #0d47a1);
}


/* เพิ่มสีสันให้ข้อความในการ์ด */
.text-white {
  color: white;
}

.text-gray-200 {
  color: #e5e5e5;
}

.tooltip {
  @apply absolute -top-10 left-1/2 transform -translate-x-1/2 w-48 bg-gray-800 text-white text-sm p-2 rounded opacity-0 group-hover:opacity-100 transition-opacity;
}

</style>


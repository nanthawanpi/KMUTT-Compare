<script setup>
import { ref, onMounted, computed } from 'vue'
import { formatDate } from '@/composables/formatDate';
import { useRouter } from 'vue-router'
import { getDormitories } from '@/composables/GetDormitories/getDormitories';
import SortComponent from '@/components/filters/SortComponent.vue';
import SearchComponent from '@/components/filters/SearchComponent.vue';
import ConfirmModal from '@/components/modals/ConfirmModal.vue';
import SuccessModal from '@/components/modals/SuccessModal.vue';

const API_ROOT = import.meta.env.VITE_API_ROOT
const router = useRouter()
const dormitories = ref([])
const searchQuery = ref('')  // ค่าของคำค้นหา

onMounted(async () => {
  dormitories.value = await getDormitories()
  // console.log(dormitories.value)
})


// Success Modal
const isModalOpen = ref(false)
const modalData = ref({ title: '', message: '', context: '' });

// Confirm Modal
const isModalVisible = ref(false);
const dormIdTodelete = ref(null)


const showDeleteModal = (dormId) => {
  dormIdTodelete.value = dormId;
  isModalVisible.value = true
}

const closeModal = () => {
  isModalOpen.value = false
  isModalVisible.value = false
  dormIdTodelete.value = null
}

// ฟังก์ชันในการกรองหอพักตามคำค้นหา
const filteredDormitories = computed(() => {
  if (!searchQuery.value) return dormitories.value; // ถ้าไม่มีการค้นหาคืนข้อมูลทั้งหมด
  return dormitories.value.filter(dorm => {
    return dorm.dormName.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
           dorm.username.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
           dorm.userId.toString().includes(searchQuery.value);
  });
});

// ลบหอพัก พร้อม Authentication
const deleteDormitory = async () => {
  const dormId = dormIdTodelete.value

  if(dormIdTodelete.value){

  try {
    const token = localStorage.getItem('token'); // ดึง token จาก localStorage
    const res = await fetch(`${API_ROOT}/dormitories/${dormIdTodelete.value}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
      }
    });

    if (res.ok) {
      dormitories.value = await getDormitories()
      modalData.value = {
          title: `ลบหอพักสำเร็จ`,
          message: `หอพัก ID: ${dormId} ถูกลบเรียบร้อยแล้ว`,
          context: 'adminDorm'
        };
        isModalOpen.value = true
    } else if (res.status === 401) {
      // ถ้า token หมดอายุหรือไม่ได้รับอนุญาต คุณอาจทำการ refresh token แล้ว retry
      alert('ไม่สามารถลบได้ เนื่องจากไม่ได้รับอนุญาต');
    } else {
      throw new Error(`HTTP error! status: ${res.status}`);
    }
  } catch (error) {
    // console.error('Error deleting dormitory:', error);
    alert('เกิดข้อผิดพลาดในการลบหอพัก');
  }
}
}

const editDormitory = (dormitoryId) => {
  router.push({
    name: 'addEditDormitory',
    params: { id: dormitoryId }
  });
};

</script>

<template>
  <div class="container mx-auto p-6 pt-20">
    <h1 class="text-3xl font-semibold mb-6 text-gray-800">จัดการหอพัก</h1>
    <div class="flex flex-row justify-center items-center space-x-2">
      <!-- ปุ่มค้นหาแบบ Real-Time -->
        <SearchComponent v-model="searchQuery"/>
      <div class="w-40">
        <SortComponent :dormitories="filteredDormitories"/>
      </div>
      
    </div>

    <div class="overflow-x-auto bg-white shadow-lg rounded-lg p-4">
      <table class="w-full table-auto border-collapse border border-gray-200">
        <thead>
          <tr class="bg-gray-100 text-gray-700">
            <th class="p-3 text-left border">#</th>
            <th class="p-3 text-left border">ชื่อหอพัก</th>
            <th class="p-3 text-left border">คะแนน</th>
            <th class="p-3 text-left border">UID เจ้าของหอพัก</th>
            <th class="p-3 text-left border">username</th>
            <th class="p-3 text-left border">ราคาต่ำสุด</th>
            <th class="p-3 text-left border">ราคาสูงสุด</th>
            <th class="p-3 text-left border">ระยะทาง</th>
            <th class="p-3 text-left border">วันที่ลงประกาศ</th>
            <th class="p-3 text-left border">อัปเดตล่าสุด</th>
            <th class="p-3 text-left border">การจัดการ</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(dorm, index) in filteredDormitories" :key="dorm.id" class="hover:bg-gray-50 transition duration-200">
            <td class="p-3 border">{{ index + 1 }}</td>
            <td class="p-3 border">{{ dorm.dormName }}</td>
            <td class="p-3 border">{{ dorm.score }}</td>
            <td class="p-3 border">{{ dorm.userId }}</td>
            <td class="p-3 border">{{ dorm.username }}</td>
            <td class="p-3 border">{{ dorm.min_price }}</td>
            <td class="p-3 border">{{ dorm.max_price }}</td>
            <td class="p-3 border">{{ dorm.distance }}</td>
            <td class="p-3 border">{{ formatDate(dorm.created_at) }}</td>
            <td class="p-3 border">{{ formatDate(dorm.updated_at) }}</td>
            <td class="p-3 border flex gap-2">
              <button @click="showDeleteModal(dorm.dormId)" class="bg-red-500 text-white py-2 px-4 rounded-lg hover:bg-red-600 transition-all">
                ลบ
              </button>
              <button @click="editDormitory(dorm.dormId)" class="bg-blue-500 text-white py-2 px-4 rounded-lg hover:bg-blue-600 transition-all">
                แก้ไข
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
  <ConfirmModal
  :isVisible="isModalVisible"
  :dormId="dormIdTodelete"
  @close="closeModal"
  @delete="deleteDormitory"
  context="delete"
  />

  <SuccessModal 
    v-if="isModalOpen" 
    :title="modalData.title" 
    :message="modalData.message" 
    :context="modalData.context" 
    @close="closeModal"
/>
</template>

<style scoped>
button {
  transition: background-color 0.3s ease, transform 0.2s ease;
}

button:hover {
  transform: translateY(-2px);
}
</style>

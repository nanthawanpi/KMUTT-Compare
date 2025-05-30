<script setup>
import Sidebar from '@/components/Sidebar.vue';
import { getUserDorm } from '@/composables/GetDormitories/getUserDorm';
import { onMounted, ref, computed } from 'vue';
import router from '@/router';
import DeleteModal from '@/components/modals/ConfirmModal.vue';
import SuccessModal from '@/components/modals/SuccessModal.vue';
import { formatPrice } from '@/composables/formatPrice';
import SortComponent from '@/components/filters/SortComponent.vue';
import SearchComponent from '@/components/filters/SearchComponent.vue';
import BorderButton from '@/components/buttons/BorderButton.vue';



const API_ROOT = import.meta.env.VITE_API_ROOT;
const dormitories = ref([]);
const isModalVisible = ref(false);
const dormIdToDelete = ref(null);

onMounted(async () => {
  dormitories.value = await getUserDorm();
});

const showDetail = (dormitoryId) => {
  router.push({
    name: 'dormitoryDetail',
    params: { id: dormitoryId }
  });
};


//---------------------------------- Search ----------------------------------
const searchInput = ref('');

const filteredDormitories = computed(() => {
  if (!searchInput.value) return dormitories.value;

  const searchTerm = searchInput.value.toLowerCase();
  return dormitories.value.filter(dorm =>
    dorm.dormName.toLowerCase().includes(searchTerm) ||
    dorm.address.street.toLowerCase().includes(searchTerm) ||
    dorm.address.subdistrict.toLowerCase().includes(searchTerm) ||
    dorm.address.district.toLowerCase().includes(searchTerm) ||
    dorm.address.province.toLowerCase().includes(searchTerm)
  );
});


//---------------------------------- Manage ----------------------------------

const editDormitory = (dormitoryId) => {
  router.push({
    name: 'addEditDormitory',
    params: { id: dormitoryId }
  });
};

const showDeleteModal = (dormId) => {
  dormIdToDelete.value = dormId;
  isModalVisible.value = true;
};

const closeModal = () => {
  isModalOpen.value = false
  isModalVisible.value = false;
  dormIdToDelete.value = null;
};

const isModalOpen = ref(false);
const modalData = ref({ title: '', message: '', context: '' });

const deleteDormitory = async () => {

  if (dormIdToDelete.value) {
    try {
      const res = await fetch(`${API_ROOT}/dormitories/${dormIdToDelete.value}`, {
        headers: {
          "Content-Type": "application/json",
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        method: 'DELETE'
      });

      if (res.ok) {
        dormitories.value = await getUserDorm();  // ดึงข้อมูลใหม่หลังการลบ
        closeModal(); 
        modalData.value = {
          title: 'ลบหอพักสำเร็จ',
          message: 'ข้อมูลหอพักถูกลบเรียบร้อยแล้ว.',
          context: 'delete'
        };
          isModalOpen.value = true;
          dormitories.value = await getUserDorm();
      } else {
        alert(`There are no dormitory with id = ${dormIdToDelete.value}`);
        throw new Error('Cannot delete data!');
      }
    } catch (error) {
      // console.log(`ERROR: ${error}`);
    }
  }
};

// Reactive state
const isPopupOpen = ref(false);
const selectedDorms = ref([]);
const email = ref('');
const isAgreed = ref(false);


const openPopup = (dormId) => {
  selectedDorms.value = [dormId];
  isPopupOpen.value = true;
};
const closePopup = () => {
  isPopupOpen.value = false;
  selectedDorms.value = []; // เคลียร์ค่าหลังปิด popup
};

const isValid = computed(() => selectedDorms.value.length > 0 && email.value && isAgreed.value);

const submitTransfer = async () => {
  if (!isValid.value) return;

  try {
    const response = await fetch(`${API_ROOT}/user/dormitory/change-user`, {
      method: 'PUT',
      headers: { 
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
       },
      body: JSON.stringify({ email: email.value, dormIds: selectedDorms.value })
    });

    if (response.ok) {
      closePopup()
      modalData.value = {
          title: 'โอนย้ายหอพักสำเร็จ',
          message: `ย้ายข้อมูลหอพักไปที่ ${email.value} เรียบร้อยแล้ว`,
          context: 'delete'
        };
          isModalOpen.value = true;
          dormitories.value = await getUserDorm();
    } else {
      if(response.status === 401){
        alert('ไม่มีอีเมลผู้ใช้นี้อยู่ในระบบ')
      }
    }
  } catch (error) {
    // console.error(error);
  }
};



</script>

<template>
  <div class="flex flex-row w-full justify-center p-20">
    <Sidebar class="hidden lg:block w-64" />

    <div class="pl-0 lg:pl-2 w-full lg:w-1/2 h-full">
      <div class="w-full flex flex-col items-center justify-center">

        <div class="flex flex-row items-center w-full space-x-2 mb-2">
          
          <SearchComponent v-model="searchInput"/>
          <div class="w-40">
            <SortComponent :dormitories="dormitories" />
          </div>
        </div>

        <div v-if="filteredDormitories.length > 0" class="space-y-4">
          <div v-for="dorm in filteredDormitories" :key="dorm.dormId" class="bg-white border rounded-2xl shadow-md overflow-hidden p-4">

          <div class="flex flex-col md:flex-row">
            <img :src="dorm.image[0] || '/images/no_image.jpg'" class="w-full md:w-1/3 h-48 md:h-auto object-cover" />
            <div class="flex flex-col justify-between p-4 w-full">
              <div>
                <div class="flex justify-between items-center mb-2">
                  <h1 class="text-lg font-bold text-orange-500 cursor-pointer" @click="showDetail(dorm.dormId)">{{ dorm.dormName }}</h1>
                  <div class="flex space-x-2">
                    <img @click="editDormitory(dorm.dormId)" src="../../../components/icons/edit.png" class="w-6 cursor-pointer" />
                    <img @click="showDeleteModal(dorm.dormId)" src="../../../components/icons/trash.png" class="w-6 cursor-pointer" />
                  </div>
                </div>
                <p class="text-green-600 font-semibold">{{ formatPrice(dorm.min_price) }} - {{ formatPrice(dorm.max_price) }} บาท/เดือน</p>
                <p>ระยะทาง: {{ dorm.distance }} กม.</p>
                <p>ประเภทหอพัก: <span>{{ dorm.type === 'all' ? 'รวม' : dorm.type === 'f' ? 'หญิง' : dorm.type === 'm' ? 'ชาย' : dorm.type }}</span></p>
                <p class="text-sm text-gray-600">ที่อยู่: {{ dorm.address.street }}, {{ dorm.address.subdistrict }}, {{ dorm.address.district }}, {{ dorm.address.province }} {{ dorm.address.postalCode }}</p>
              </div>
              <BorderButton @click="openPopup(dorm.dormId)" class="mt-4 w-full sm:w-auto" context="โอนย้ายหอพัก" />
            </div>
          </div>
        </div>
      </div>
      <div v-else class="text-center text-gray-600 mt-5">ยังไม่มีหอพักที่ประกาศไว้</div>
    </div>
    </div>
  </div>

  <div v-if="isPopupOpen" class="fixed pt-24 inset-0 flex items-center justify-center bg-black bg-opacity-50">
  <div class="bg-white rounded-xl p-8 w-full max-w-lg shadow-2xl">
    <h2 class="text-3xl font-bold text-center text-gray-800">เลือกหอพักที่ต้องการโอนย้าย</h2>

    <!-- รายการหอพัก -->
    <div class="mt-6 space-y-3 text-lg max-h-60 overflow-y-auto p-2 border rounded-lg">
      <label v-for="dorm in dormitories" :key="dorm.dormId" class="flex items-center space-x-3">
        <input type="checkbox" :value="dorm.dormId" v-model="selectedDorms" class="w-5 h-5 text-blue-500" />
        <span class="text-gray-700">{{ dorm.dormName }}</span>
      </label>
    </div>

    <!-- กรอกอีเมล -->
    <div class="mt-6">
      <input v-model="email" type="email" placeholder="กรอกอีเมล" class="text-lg border border-gray-300 p-3 w-full rounded-lg shadow-sm" />
    </div>

    <!-- ข้อตกลง -->
    <div class="mt-4 p-5 bg-gray-100 rounded-lg text-lg shadow-inner">
      <p class="font-semibold">📌 รายละเอียดข้อตกลง:</p>
      <ul class="list-disc list-inside text-gray-600 space-y-2 mt-2">
        <li>ผู้โอนสามารถดำเนินการโอนย้ายเจ้าของหอพักได้โดยไม่ต้องได้รับความยินยอมจากผู้รับโอน</li>
        <li>ผู้โอนต้องตรวจสอบและยืนยันข้อมูลให้ถูกต้องก่อนโอนย้าย</li>
        <li>เมื่อโอนย้ายเสร็จสมบูรณ์ ผู้โอนต้องรับผิดชอบหากเกิดปัญหาหรือข้อผิดพลาดจากการโอนย้าย</li>
      </ul>
    </div>

    <!-- Checkbox ยอมรับข้อตกลง -->
    <label class="flex items-center mt-4">
      <input type="checkbox" v-model="isAgreed" class="w-5 h-5 text-blue-500" />
      <span class="ml-3 text-gray-700">ยอมรับข้อตกลง</span>
    </label>

    <!-- ปุ่ม -->
    <div class="flex justify-end mt-6 space-x-3">
      <button @click="closePopup" class="border px-5 py-3 rounded-lg text-gray-700 hover:bg-gray-100 transition">ยกเลิก</button>
      <button @click="submitTransfer" :disabled="!isValid" class="bg-blue-600 text-white px-5 py-3 rounded-lg shadow-md hover:bg-blue-700 disabled:opacity-50 transition">ยืนยัน</button>
    </div>
  </div>
</div>


  <!-- Modal for deletion -->
    <!-- DeleteModal for deletion -->
    <DeleteModal 
    :isVisible="isModalVisible" 
    :dormId="dormIdToDelete" 
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

/* ภาพพื้นหลังด้านบน */
.background img {
  width: 100%;
  height: 400px; 
  object-fit: cover; 
}

.item h1 {
  font-size: 1.5rem; /* ขนาดเริ่มต้นสำหรับหน้าจอขนาดเล็ก */
  color: #F4845F;
}

.item h1:hover {
  color: #dd5c2d; /* เปลี่ยนสีเมื่อ hover */
  transform: scale(1.02); /* ขยายขนาดเล็กน้อยเมื่อ hover */
}

.item h2 {
  font-size: 1rem;
  color: black;
}

.item h2 span {
  font-size: 0.9rem;
  color: #5D5D5D;
}

.item p {
  font-size: 0.9rem;
  color: #5D5D5D;
}

/* สำหรับหน้าจอขนาดกลาง (เช่น Tablet) */
@media (min-width: 640px) {
  .item h1 {
    font-size: 1.75rem;
  }
  
  .item h2 {
    font-size: 1.2rem;
  }
  
  .item h2 span {
    font-size: 1rem;
  }
  
  .item p {
    font-size: 0.8rem;
  }
}

/* สำหรับหน้าจอขนาดใหญ่ (เช่น Desktop) */
@media (min-width: 1024px) {
  .item h1 {
    font-size: 1.rem;
  }
  
  .item h2 {
    font-size: 1.2rem;
  }
  
  .item h2 span {
    font-size: 1.2rem;
  }
  
  .item p {
    font-size: 1rem;
  }
}
.price-select {
  padding: 8px;
  border: 2px solid #ccc; 
  box-sizing: border-box; 
  cursor: pointer;
  width: 250px;
}

hr{
  width: 250px;
}

.filter h2{
  font-size: 1.2rem;
}

.popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.filter {
  background: white;
  padding: 20px;
  padding-left: 40px;
  border-radius: 8px;
  width: 700px;
  max-width: 100%;
  height: 95%;
  position: relative;
}


@media screen and (max-width: 768px) {
  .filter {
    padding: 20px;
    width: 70%;
    height: 90%;
    font-size: 0.8rem;
  }

  .btn {
    width: 20%;
  }
}

.icons{
    display: flex;
    flex-direction: row;
    width: 100%;
  
}

.icons img{
    width: 40px;
    margin-left: 5px;
}


/* ไอคอน*/
.icons div:hover img {
  transform: scale(1.1); /* ขยายขนาดเล็กน้อยเมื่อ hover */
  transition: transform 0.3s ease; /* เพิ่มการขยายขนาดอย่างนุ่มนวล */
}

.popup {
  background: white;
  padding: 20px;
  border-radius: 8px;
  width: 400px;
}


</style>
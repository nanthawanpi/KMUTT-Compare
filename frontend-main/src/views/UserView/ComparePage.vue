<script setup>
import { storeToRefs } from 'pinia';
import { useCompareStore } from '@/stores/compareStore';
import { computed, ref, onMounted } from 'vue';
import { getDormitories } from '@/composables/GetDormitories/getDormitories';
import SearchComponent from '@/components/filters/SearchComponent.vue';

const compareStore = useCompareStore();
const { compareItems } = storeToRefs(compareStore);

const dormitories = ref([])

onMounted(async () => {
  dormitories.value = await getDormitories();

})


// ฟังก์ชันแปลงที่อยู่ให้เป็นข้อความ
const formatAddress = (address) => {
  return `${address.dormNumber} ${address.street}, ${address.subdistrict}, ${address.district}, ${address.province}, ${address.postalCode}`;
};

// ฟังก์ชันเปรียบเทียบสิ่งอำนวยความสะดวก
const getFacilityStatus = (facility, dormFacilities) => {
  return dormFacilities.includes(facility) ? '✅' : '❌';
};

// ฟังก์ชันที่ใช้เปรียบเทียบค่าระหว่างสองหอพัก
const compareDorms = (dorm1, dorm2) => {
  return {
    min_price: dorm1.min_price <= dorm2.min_price,  // ถ้าราคาเริ่มต้นของหอพัก 1 ต่ำกว่าหอพัก 2
    max_price: dorm1.max_price <= dorm2.max_price,  // ถ้าราคาสูงสุดของหอพัก 1 สูงกว่าหอพัก 2
    size: dorm1.size >= dorm2.size,                // ถ้าขนาดห้องของหอพัก 1 ใหญ่กว่าหอพัก 2
    distance: dorm1.distance <= dorm2.distance      // ถ้าระยะทางของหอพัก 1 ใกล้กว่าหอพัก 2
  };
};

// เปรียบเทียบหอพักทั้งสอง
const dorm1Comparison = computed(() => compareDorms(compareItems.value[0], compareItems.value[1]));
const dorm2Comparison = computed(() => compareDorms(compareItems.value[1], compareItems.value[0]));

const isOpenModal = ref(false)

const chosenDorm = ref(0)

// ฟังก์ชันเลือกหอพัก
const changeDorm = (index) => {
  chosenDorm.value = index;  // กำหนดว่ากำลังเปลี่ยนหอพักที่ index ไหน (0 หรือ 1)
  isOpenModal.value = true;  // เปิด Modal ให้เลือกหอพักใหม่
};

// ฟังก์ชันเลือกหอพัก
const selectDorm = (dorm) => {
  // ตรวจสอบว่าต้องการแทนที่หอพักที่ตำแหน่งไหน
  if (chosenDorm.value === 0) {
    compareStore.replaceDormInCompare(0, dorm); 
  } else if (chosenDorm.value === 1) {
    compareStore.replaceDormInCompare(1, dorm); 
  }

  isOpenModal.value = false; // ปิด Modal
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


const currentPage = ref(1);
const itemsPerPage = ref(5);

const paginatedDormitories = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  const end = start + itemsPerPage.value;
  return filteredDormitories.value.slice(start, end);
});

const totalPages = computed(() => Math.ceil(filteredDormitories.value.length / itemsPerPage.value));

const nextPage = () => {
  if (currentPage.value < totalPages.value) currentPage.value++;
};

const prevPage = () => {
  if (currentPage.value > 1) currentPage.value--;
};

</script>

<template>
  <div class="w-full min-h-screen flex flex-col bg-gray-50 py-6">
    
    <div class="container mx-auto grid grid-cols-2 gap-6">
      <!-- หอพักซ้าย -->
      <div class="bg-white p-6 rounded-lg shadow-md relative">
        <img :src="compareItems[0]?.image[0] || '/images/no_image.jpg'" alt="Dorm 1 Image" class="w-full h-52 object-cover mb-4 rounded-lg" />
        
        <!-- Rating ที่จะอยู่มุมขวาบนของรูป -->
        <div class="absolute top-2 right-2 bg-white px-2 py-1 rounded-lg shadow-md flex items-center justify-center space-x-1">
          <img src="/star.png" alt="Star" class="w-6">
          <strong v-if="compareItems[0]?.rating?.totalScore && compareItems[0].rating.totalScore !== 0" class="text-gray-800">
          {{ compareItems[0].rating.totalScore }}
          </strong>
          <strong v-else class="text-gray-800">ยังไม่มีคะแนน</strong>
        </div>

        <div class="flex flex-row items-center justify-center space-x-2">
          <h2 class="text-xl font-bold text-gray-800 text-center">{{ compareItems[0]?.dormName }}</h2>
          <div class="flex items-center justify-end">
            <button @click="changeDorm(0)" class="underline text-custom">เปลี่ยน</button>
          </div>
        </div>
        <ul class="mt-4 space-y-2">
          <li><strong>ที่อยู่:</strong> {{ formatAddress(compareItems[0]?.address) }}</li>
          <li>
            <span v-if="dorm1Comparison.min_price">✅</span>
            <span v-else>❌</span>
            <strong>ราคาเริ่มต้น:</strong> 
            {{ compareItems[0]?.min_price }} บาท
          </li>
        <li>
          <span v-if="dorm1Comparison.max_price">✅</span>
          <span v-else>❌</span>
          <strong>ราคาสูงสุด:</strong> 
          {{ compareItems[0]?.max_price }} บาท
        </li>
        <li>
          <span v-if="dorm1Comparison.size">✅</span>
          <span v-else>❌</span>
          <strong>ขนาดห้อง:</strong> 
          {{ compareItems[0]?.size }} ตร.ม.
        </li>
        <li>
          <span v-if="dorm1Comparison.distance">✅</span>
          <span v-else>❌</span>
          <strong>ระยะทาง:</strong> 
          {{ compareItems[0]?.distance }} กม.
        </li>
        </ul>
      </div>

      <!-- หอพักขวา -->
      <div class="bg-white p-6 rounded-lg shadow-md relative">
        <img :src="compareItems[1]?.image[0] || '/images/no_image.jpg'" alt="Dorm 2 Image" class="w-full h-52 object-cover mb-4 rounded-lg" />
        
  
      <!-- Rating ที่จะอยู่มุมขวาบนของรูป -->
      <div class="absolute top-2 right-2 bg-white px-2 py-1 rounded-lg shadow-md flex items-center justify-center space-x-1">
        <img src="/star.png" alt="Star" class="w-6">
        <strong v-if="compareItems[1]?.rating?.totalScore && compareItems[1].rating.totalScore !== 0" class="text-gray-800">
          {{ compareItems[1].rating.totalScore }}
          </strong>
          <strong v-else class="text-gray-800">ยังไม่มีคะแนน</strong>
      </div>


        <div class="flex flex-row items-center justify-center space-x-2">
          <h2 class="text-xl font-bold text-gray-800 text-center">{{ compareItems[1]?.dormName }}</h2>
          <div class="flex items-center justify-end">
            <button @click="changeDorm(1)" class="underline text-custom">เปลี่ยน</button>
          </div>
        </div>
        <ul class="mt-4 space-y-2">
          <li><strong>ที่อยู่:</strong> {{ formatAddress(compareItems[1]?.address) }}</li>
          <li>
            <span v-if="dorm2Comparison.min_price">✅</span>
            <span v-else>❌</span>
            <strong>ราคาเริ่มต้น:</strong> 
            {{ compareItems[1]?.min_price }} บาท
        </li>
        <li>
          <span v-if="dorm2Comparison.max_price">✅</span>
          <span v-else>❌</span>
          <strong>ราคาสูงสุด:</strong> 
          {{ compareItems[1]?.max_price }} บาท
        </li>
        <li>
          <span v-if="dorm2Comparison.size">✅</span>
          <span v-else>❌</span>
          <strong>ขนาดห้อง:</strong> 
          {{ compareItems[1]?.size }} ตร.ม.
        </li>
        <li>
          <span v-if="dorm2Comparison.distance">✅</span>
          <span v-else>❌</span>
          <strong>ระยะทาง:</strong> 
          {{ compareItems[1]?.distance }} กม.
        </li>
        </ul>
      </div>
    </div>

    <!-- ตารางเปรียบเทียบสิ่งอำนวยความสะดวก -->
    <div class="container mx-auto mt-8">
      <h3 class="text-lg font-bold text-center mb-4">เปรียบเทียบสิ่งอำนวยความสะดวก</h3>
      <div class="bg-white p-6 rounded-lg shadow-md">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-gray-100">
              <th class="p-3 border">สิ่งอำนวยความสะดวกภายในห้องพัก</th>
              <th class="p-3 border text-center">{{ compareItems[0]?.dormName }}</th>
              <th class="p-3 border text-center">{{ compareItems[1]?.dormName }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="facility in [...new Set([...compareItems[0]?.room_facility, ...compareItems[1]?.room_facility])]" :key="facility">
              <td class="p-3 border">{{ facility }}</td>
              <td class="p-3 border text-center">{{ getFacilityStatus(facility, compareItems[0]?.room_facility) }}</td>
              <td class="p-3 border text-center">{{ getFacilityStatus(facility, compareItems[1]?.room_facility) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="bg-white p-6 rounded-lg shadow-md">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-gray-100">
              <th class="p-3 border">สิ่งอำนวยความสะดวกภายนอกห้องพัก</th>
              <th class="p-3 border text-center">{{ compareItems[0]?.dormName }}</th>
              <th class="p-3 border text-center">{{ compareItems[1]?.dormName }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="facility in [...new Set([...compareItems[0]?.building_facility, ...compareItems[1]?.building_facility])]" :key="facility">
              <td class="p-3 border">{{ facility }}</td>
              <td class="p-3 border text-center">{{ getFacilityStatus(facility, compareItems[0]?.building_facility) }}</td>
              <td class="p-3 border text-center">{{ getFacilityStatus(facility, compareItems[1]?.building_facility) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

  </div>

  <!-- Modal -->
  <div v-if="isOpenModal" class="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
    <div class="bg-white p-6 rounded-lg w-96 relative">
      <!-- ปุ่มปิดกากบาท -->
      <button 
        @click="isOpenModal = false" 
        class="absolute top-2 right-2 text-gray-600 hover:text-gray-900 text-xl">
        ✖
      </button>

      <h3 class="text-xl font-bold text-center mb-4">เลือกหอพัก</h3>
      <SearchComponent v-model="searchInput"/>

      <div class="space-y-4 border" v-for="dorm in paginatedDormitories" :key="dorm.dormId">
        <div @click="selectDorm(dorm)" class="flex flex-row  items-center justify-between block w-full text-left p-2 mb-2 rounded hover:bg-gray-300"> 
          <div class="text-lg">{{ dorm.dormName }}</div>
          <div><img :src="dorm?.image[0] || '/images/no_image.jpg'" class="h-10 bg-cover bg-center rounded-lg" alt="Dormitory Image"/></div>
        </div>
      </div>

      <div class="flex justify-center items-center space-x-3 mt-4">
        <button @click="prevPage" :disabled="currentPage === 1" class="px-4 py-2 bg-gray-300 rounded">
          <
        </button>
          <span>หน้าที่ {{ currentPage }} / {{ totalPages }}</span>
        <button @click="nextPage" :disabled="currentPage === totalPages" class="px-4 py-2 bg-gray-300 rounded">
          >
        </button>
      </div>

    </div>
  </div>

</template>

<style scoped>
.container {
  max-width: 1200px;
}

strong{
  padding-left: 5px;
}

.underline {
  text-decoration: underline;
}

.text-custom {
  color: #FF5733; /* Replace with your preferred color */
}
</style>
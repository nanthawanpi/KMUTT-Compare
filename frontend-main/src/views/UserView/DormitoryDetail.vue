<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { formatDate } from '@/composables/formatDate';
import { getDormitoryById } from '@/composables/GetDormitories/getDormitoryById';
import { formatPrice } from '@/composables/formatPrice';
import router from '@/router';
import { formatPhoneNumber } from '@/composables/formatPhoneNumber';
import CompareButton from '@/components/buttons/CompareButton.vue';
import { storeToRefs } from 'pinia';
import { useCompareStore } from '@/stores/compareStore'; // นำเข้าจาก store ที่สร้างไว้
import BorderButton from '@/components/buttons/BorderButton.vue';
import { useUIStore } from '@/stores/uiStore';
const compareStore = useCompareStore();
const { compareItems } = storeToRefs(compareStore);
// console.log(compareItems.value)

const addDormDetailToCompare = (dormitoryDetaill) => {
  compareStore.addDormDetailToCompare(dormitoryDetaill);
};


const dormImages = ref([]);

const showModal = ref(false);
const selectedImage = ref("");

const { params } = useRoute();
const dormitoryDetaill = ref([]);

// ฟังก์ชันเปิดปิดโมดัล
function openModal(image) {
  selectedImage.value = image;
  showModal.value = true;
}

function closeModal() {
  showModal.value = false;
}


onMounted(async () => {
  try {
    dormitoryDetaill.value = await getDormitoryById(params.id);
    // console.log(dormitoryDetaill.value)

     // กำหนดค่า dormImages จากข้อมูลที่ได้จาก server
     dormImages.value = dormitoryDetaill.value.image;
     
    if (dormitoryDetaill.value.address) {
      const convertedAddress = convertAddressForGeocoding(dormitoryDetaill.value.address);  // แปลงที่อยู่ก่อน
      initMap(convertedAddress); // ส่งที่อยู่ที่แปลงแล้วไปยัง initMap
    }
  } catch (error) {
    // console.error('Error fetching dormitory:', error);
  }
});

function convertAddressForGeocoding(address) {
  const addressObject = {
    subpremise: '', // ตัวแปรที่จะเก็บเลขที่ห้อง/เลขที่
    street_number: '', // ตัวแปรที่จะเก็บหมายเลขถนน
    route: '', // ตัวแปรที่จะเก็บชื่อถนน
    sublocality_level_2: '', // ตัวแปรที่จะเก็บแขวง
    sublocality_level_1: '', // ตัวแปรที่จะเก็บเขต
    administrative_area_level_1: '', // ตัวแปรที่จะเก็บจังหวัด
    country: '', // ตัวแปรที่จะเก็บประเทศ
    postal_code: '' // ตัวแปรที่จะเก็บรหัสไปรษณีย์
  };

  // ตรวจสอบว่า address.dormNumber มีค่าและไม่เป็น null หรือ undefined ก่อนการใช้งาน
  if (address.dormNumber && address.dormNumber.includes('/')) {
    // แยกเลขที่ห้องและหมายเลขถนน
    const dormNumberParts = address.dormNumber.split('/');
    addressObject.subpremise = dormNumberParts[0].trim(); // เลขที่ห้อง/เลขที่
    addressObject.street_number = dormNumberParts[1] ? dormNumberParts[1].trim() : ''; // หมายเลขถนน
  } else {
    // ถ้าไม่มีเครื่องหมาย / ให้เก็บหมายเลขถนนลงใน street_number
    addressObject.street_number = address.dormNumber ? address.dormNumber.trim() : '';
  }

  // กำหนดค่าของแต่ละตัวแปร
  addressObject.route = address.street || ''; // ชื่อถนน
  addressObject.sublocality_level_2 = address.subdistrict || ''; // แขวง
  addressObject.sublocality_level_1 = address.district || ''; // เขต
  addressObject.administrative_area_level_1 = address.province || ''; // จังหวัด
  addressObject.postal_code = address.postalCode || ''; // รหัสไปรษณีย์
  addressObject.country = address.country || ''; // ประเทศ

  return addressObject;
}


function initMap(addressObject) {
  const geocoder = new google.maps.Geocoder();

  // สร้าง addressString ด้วยการใช้ข้อมูลจาก addressObject ที่แปลงแล้ว
  const addressString = `${addressObject.subpremise || ''} ${addressObject.street_number || ''} ${addressObject.route}, ${addressObject.sublocality_level_2}, ${addressObject.sublocality_level_1}, ${addressObject.administrative_area_level_1}, ${addressObject.postal_code}, ${addressObject.country}`;

  // console.log("Address String for Geocoding:", addressString);  // ตรวจสอบคำขอที่ส่งไปยัง Geocoding API

  geocoder.geocode({ address: addressString }, (results, status) => {
    if (status === google.maps.GeocoderStatus.OK) {
      const locationA = results[0].geometry.location;  // ตำแหน่งที่ได้จาก Geocoding

      // console.log("Geocoding results for A:", results);  // ดูผลลัพธ์ที่ได้จาก Geocoding API

      // สร้างแผนที่
      const map = new google.maps.Map(document.getElementById("map"), {
        center: locationA,
        zoom: 15,
      });

      // เพิ่ม Marker สำหรับจุด A (ที่อยู่ปัจจุบัน)
      const markerA = new google.maps.Marker({
        position: locationA,
        map: map,
        title: "Current Location",
      });

      // เพิ่ม Marker สำหรับจุด B (ที่อยู่ KMUTT)
      const kmuttLocation = { lat: 13.651309958082942, lng: 100.49645730815111 };
      const markerB = new google.maps.Marker({
        position: kmuttLocation,
        map: map,
        title: "KMUTT Location",
      });

      // เชื่อมต่อจุด A และ B ด้วยเส้น
      const directionsService = new google.maps.DirectionsService();
      const directionsRenderer = new google.maps.DirectionsRenderer({
        map: map,
      });

      const request = {
        origin: locationA,
        destination: kmuttLocation,
        travelMode: google.maps.TravelMode.DRIVING, // สามารถเปลี่ยนเป็น WALKING, BICYCLING, หรือ TRANSIT
      };

      // คำนวณเส้นทางระหว่างจุด A และ B
      directionsService.route(request, (result, status) => {
        if (status === google.maps.DirectionsStatus.OK) {
          directionsRenderer.setDirections(result);
        } else {
          // console.error("Error with DirectionsService: " + status);
        }
      });

    } else {
      alert('ไม่สามารถค้นหาที่อยู่ได้: ' + status);
      // console.error('Geocoding failed with status: ' + status);  // แสดงข้อผิดพลาดในกรณีที่การค้นหาล้มเหลว
    }
  });
}

//---------------------------------- Reservation ----------------------------------

// ดูรายละเอียดหอพัก
const reserveDorm = (dormitoryId) => {
  // ตรวจสอบว่า token มีอยู่ใน localStorage หรือไม่
  const token = localStorage.getItem('token');
  if (!token) {
    // ถ้าไม่มี token ให้เรียก openLoginPopup
    const uiStore = useUIStore();  // ใช้ store เพื่อเรียกฟังก์ชัน openLoginPopup
    uiStore.openLoginPopup();
  } else {
    // ถ้ามี token ให้ไปที่หน้าจองหอพัก
    router.push({
      name: 'reservation',
      params: { id: dormitoryId, action: 'add' }
    });
  }
};


</script>

<template>

  <div class="w-full h-full flex justify-center bg-gray-100 py-8">
    <div class="flex flex-col border border-gray-300 rounded-lg shadow-lg w-3/4 bg-white p-6">
      <!-- เมนูด้านบน -->
      <div class="flex text-gray-700 px-6 justify-between text-lg">
        <p>ประกาศโดย : {{ dormitoryDetaill.username }}</p>
        <p>สถานะ : 
          {{ dormitoryDetaill.status === 'empty' ? 'ว่าง' : dormitoryDetaill.status === 'full' ? 'ไม่ว่าง' : dormitoryDetaill.status }}
        </p>
        <p>อัปเดตล่าสุด : {{ formatDate(dormitoryDetaill.updated_at) }}</p>
      </div>

      <!-- รายละเอียดมุมขวาบน -->
    <div class="flex flex-col md:flex-row md:space-x-4 p-6 rounded-lg">
      <!-- กรอบสำหรับรูป 3 รูปทางซ้าย -->
      <div class="w-full md:w-1/5 flex flex-col space-y-2">
        <!-- ตรวจสอบว่ามีรูปใน dormImages[1] หรือไม่ -->
        <img  
          class="imgs object-cover w-full h-36 rounded-lg shadow-md hover:scale-105 transition-transform" 
          :src="dormImages[1] || '/images/no_image.jpg'" 
          alt="Image 1">

        <!-- ตรวจสอบว่ามีรูปใน dormImages[2] หรือไม่ -->
        <img  
          class="imgs object-cover w-full h-36 rounded-lg shadow-md hover:scale-105 transition-transform" 
          :src="dormImages[2] || '/images/no_image.jpg'" 
          alt="Image 2">

        <!-- ตรวจสอบว่ามีรูปใน dormImages[3] หรือไม่ -->
        <div class="relative">
          <img 
            class="imgs object-cover w-full h-36 rounded-lg shadow-md hover:scale-105 transition-transform" 
            :src="dormImages[3] || '/images/no_image.jpg'" 
            alt="Image 3">
          <!-- ป้ายดูเพิ่มเติม -->
          <div 
            class="absolute top-0 left-0 w-full h-full flex justify-center items-center bg-black bg-opacity-50 text-white font-semibold text-xl cursor-pointer" 
            @click="openModal(dormImages)">
            ดูเพิ่มเติม
          </div>
        </div>
      </div>

      <!-- กรอบสำหรับรูปเดียวทางขวา -->
      <div class="first-img w-full md:w-3/6 flex justify-center">
        <!-- ตรวจสอบว่ามีรูปใน dormImages[0] หรือไม่ -->
        <img 
          class="imgs object-cover w-full rounded-lg shadow-lg hover:scale-105 transition-transform" 
          :src="dormImages[0] || '/images/no_image.jpg'" 
          alt="Main Image">
      </div>


        <!-- รายละเอียดหอพัก -->
        <div class="w-full md:w-3/6 space-y-4 p-4 rounded-lg shadow-md">
          <h1 class="text-5xl font-semibold">{{ dormitoryDetaill.dormName }}</h1>
          <div class="flex flex-row space-x-2 mt-5">
                  <img class="w-6" src="/star.png" alt="">
                  <h2 v-if="dormitoryDetaill.rating && dormitoryDetaill.rating.totalScore !== 0">
                    {{ dormitoryDetaill.rating.totalScore }}
                  </h2>
                  <h2 v-else>ยังไม่มีคะแนน</h2>

              </div>
          <div class="pt-4">
            <p class="font-semibold">ราคา</p>
            <h2 class="text-2xl font-semibold"><span class="text-4xl text-green-500 font-semibold">{{ formatPrice(dormitoryDetaill.min_price) }} - {{ formatPrice(dormitoryDetaill.max_price) }}</span> บาท / เดือน</h2>
          </div>
          <div class="flex flex-col space-y-2 pt-4">
            <BorderButton class="bg-orange-500 text-white" @click="reserveDorm(dormitoryDetaill.dormId)" context="จองหอพัก" />
            <BorderButton @click="addDormDetailToCompare(dormitoryDetaill)" context="เพิ่มลงในรายการเปรียบเทียบ" />
          </div>
        </div>
      </div>

      <!-- รายละเอียดต่างๆ -->
      <div class="flex flex-col space-y-6 px-6">
        <h2 class="text-3xl font-semibold pt-8">รายละเอียดหอพัก</h2>

        <div class="flex flex-wrap gap-4">
          <!-- กล่องที่ 1: จำนวนห้องพัก -->
          <div class="flex-1 min-w-[200px] p-4 border rounded-lg shadow-md bg-white">
            <p class="font-medium">จำนวนห้องพักที่เหลือให้เช่า:</p>
            <p class="text-lg font-semibold text-green-500">{{ dormitoryDetaill.roomCount }} ห้อง</p>
          </div>

          <!-- กล่องที่ 2: ประเภทหอพัก -->
          <div class="flex-1 min-w-[200px] p-4 border rounded-lg shadow-md bg-white">
            <p class="font-medium">ประเภทหอพัก:</p>
            <p class="text-lg font-semibold text-green-500">
              <span v-if="dormitoryDetaill.type === 'f'">หญิง</span>
              <span v-else-if="dormitoryDetaill.type === 'm'">ชาย</span>
              <span v-else-if="dormitoryDetaill.type === 'all'">รวม</span>
            </p>
          </div>

          <!-- กล่องที่ 3: ขนาดห้อง -->
          <div class="flex-1 min-w-[200px] p-4 border rounded-lg shadow-md bg-white">
            <p class="font-medium">ขนาดห้อง:</p>
            <p class="text-lg font-semibold text-green-500">{{ dormitoryDetaill.size }} ตร.ม.</p>
          </div>
        </div>

        
         <!-- Facility: ภายในห้องพัก และ ภายนอกอาคาร -->
  <div class="flex space-x-4 pt-8">
    <!-- ภายในห้องพัก -->
    <div class="w-full md:w-1/2 space-y-4">
      <h3 class="text-2xl font-semibold">สิ่งอำนวยความสะดวกภายในห้องพัก</h3>
      <div class="overflow-y-auto" style="max-height: 300px;">
        <table class="min-w-full table-auto text-left border-collapse">
          <thead>
            <tr class="bg-gray-200">
              <th class="px-4 py-2 font-semibold text-xl text-gray-700">รายการสิ่งอำนวยความสะดวก</th>
            </tr>
          </thead>
          <tbody>
            <!-- เช็คจำนวนสิ่งอำนวยความสะดวกในทั้งสองฝั่ง -->
            <tr v-for="(facility, index) in dormitoryDetaill.room_facility" :key="index">
              <td class="border px-4 py-2"><p>{{ facility }}</p></td>
            </tr>
            <!-- ถ้าไม่มีสิ่งอำนวยความสะดวกในฝั่งนี้ ให้ใส่แถวที่ว่าง -->
            <tr v-if="dormitoryDetaill?.room_facility?.length < dormitoryDetaill?.building_facility?.length">
              <td class="border px-4 py-2">&nbsp;</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ภายนอกอาคาร -->
    <div class="w-full md:w-1/2 space-y-4">
      <h3 class="text-2xl font-semibold">สิ่งอำนวยความสะดวกภายใน และ ภายนอกอาคาร</h3>
      <div class="overflow-y-auto" style="max-height: 300px;">
        <table class="min-w-full table-auto text-left border-collapse">
          <thead>
            <tr class="bg-gray-200">
              <th class="px-4 py-2 font-semibold text-xl text-gray-700">รายการสิ่งอำนวยความสะดวก</th>
            </tr>
          </thead>
          <tbody>
            <!-- เช็คจำนวนสิ่งอำนวยความสะดวกในทั้งสองฝั่ง -->
            <tr v-for="(facility, index) in dormitoryDetaill?.building_facility" :key="index">
              <td class="border px-4 py-2"><p>{{ facility }}</p></td>
            </tr>
            <!-- ถ้าไม่มีสิ่งอำนวยความสะดวกในฝั่งนี้ ให้ใส่แถวที่ว่าง -->
            <tr v-if="dormitoryDetaill?.building_facility?.length < dormitoryDetaill?.room_facility?.length">
              <td class="border px-4 py-2">&nbsp;</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
  
<!-- ที่อยู่ -->
<div class="grid grid-cols-1 md:grid-cols-2 gap-4 pt-8">
  
  <div>
    <h3 class="text-2xl font-semibold mb-2">ที่อยู่หอพัก</h3>
      <p v-for="(address, index) in dormitoryDetaill" :key="index">
        {{ address.dormNumber }} {{ address.street }} {{ address.subdistrict }} {{ address.district }} {{ address.province }} {{ address.postalCode }}
      </p>
  </div>

  <!-- เบอร์ติดต่อ -->
  <div>
  <h3 class="text-2xl font-semibold mb-2">เบอร์ติดต่อ</h3>
  <p>{{ dormitoryDetaill.phone ? formatPhoneNumber(dormitoryDetaill.phone) : 'ไม่มีเบอร์โทร' }}</p>
</div>


</div>



        <h3 class="text-2xl font-semibold pt-8">ห่างกับมหาวิทยาลัยพระจอมเกล้าธนบุรีประมาณ : {{dormitoryDetaill.distance }} กม.</h3>

          <!-- แผนที่ -->
        <div class="w-full h-80 my-8">
          <div id="map" class="w-full h-full rounded-lg shadow-lg"></div>
        </div>

      </div>
    </div>
  </div>

  

<!-- Modal แสดงรูปภาพเพิ่มเติม -->
<div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
  <div class="bg-white p-4 rounded-lg w-4/5 h-4/5 overflow-auto">
    <div class="flex justify-end">
      <button @click="closeModal" class="text-xl font-bold text-gray-700">X</button>
    </div>
    <div v-if="dormImages.length > 0" class="grid grid-cols-3 gap-4">
      <div v-for="(image, index) in dormImages" :key="index" class="relative">
        <img :src="image" alt="Gallery Image" class="w-full h-48 object-cover rounded-lg shadow-md">
      </div>
    </div>
    <div v-else class="flex items-center justify-center h-full">
      <div class="text-4xl text-gray-700">ไม่มีรูปภาพในแกลลอรี่</div>
    </div>
  </div>
</div>

<!-- แสดงปุ่มการเปรียบเทียบ -->
<div v-if="compareItems.length > 0" class="fixed bottom-4 right-4">
  <CompareButton/>
</div>

</template>


<style scoped>


.bg-opacity-50 {
  background-color: rgba(0, 0, 0, 0.5);
}

.z-50 {
  z-index: 50;
}

/* เพิ่มเอฟเฟกต์สำหรับการคลิก */
.imgs {
  cursor: pointer;
  transition: transform 0.3s ease;
}

.imgs:hover {
  transform: scale(1.05);
}

.first-img{
  height: 448px;
}

/* ปรับแต่งตาราง facility */
table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 12px;
  border: 1px solid #ddd;
}

th {
  background-color: #f5f5f5;
}

td {
  text-align: left;
}

.flex {
  display: flex;
}

.space-x-4 {
  gap: 1rem;
}

p{
  font-size: 1.2rem;
}



@keyframes bounce {
  0% { transform: translateY(0); }
  30% { transform: translateY(-10px); }
  50% { transform: translateY(0); }
  70% { transform: translateY(-5px); }
  100% { transform: translateY(0); }
}

.fixed-booking-button {
  position: fixed;
  bottom: 20px;
  right: 20px;
  background-color: #ff6600;
  color: white;
  padding: 12px 24px;
  border-radius: 50px;
  font-size: 1.2rem;
  font-weight: bold;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  z-index: 100;
  transition: transform 0.3s ease, background-color 0.3s ease, box-shadow 0.3s ease;
}

.fixed-booking-button:hover {
  transform: scale(1.1); /* ทำให้ปุ่มขยาย */
  background-color: #e65c00; /* เปลี่ยนสีเมื่อ hover */
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.3); /* เพิ่มเงาให้ดูเด่น */
}

.fixed-booking {
  position: fixed;
  bottom: 70px;
  right: 14px;
  font-size: 1.2rem;
  font-weight: bold;
  cursor: pointer;
  z-index: 100;
}

</style>

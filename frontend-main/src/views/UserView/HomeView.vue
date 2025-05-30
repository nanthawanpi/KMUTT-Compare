<script setup>
import { ref, onMounted, computed,watch } from 'vue'
import router from '@/router';
import { getDormitories } from '@/composables/GetDormitories/getDormitories';
import { formatPrice } from '@/composables/formatPrice';
import { getFavorites } from '@/composables/getFavorites';
import SortComponent from '@/components/filters/SortComponent.vue';
import { useAuthorize } from '@/stores/authorize';
import { storeToRefs } from 'pinia';
import CompareButton from '@/components/buttons/CompareButton.vue';
import BorderButton from '@/components/buttons/BorderButton.vue';
import { useCompareStore } from '@/stores/compareStore'; // นำเข้าจาก store ที่สร้างไว้
import SearchComponent from '@/components/filters/SearchComponent.vue';

const myRole = useAuthorize()
const {userRole} = storeToRefs(myRole)

const API_ROOT = import.meta.env.VITE_API_ROOT
const dormitories = ref([])
const favorites = ref([]); // เก็บรายการโปรด
const compareStore = useCompareStore();
const { compareItems } = storeToRefs(compareStore);

const addDormToCompare = (id) => {
  compareStore.addDormToCompare(id, dormitories.value);
};



onMounted(async () => {
  dormitories.value = await getDormitories();

  if(userRole.value !== 'guest'){
    favorites.value = await getFavorites();
  }
})


// ดูรายละเอียดหอพัก
const showDetail = (dormitoryId) =>{
  router.push({
    name : 'dormitoryDetail',
    params : {id : dormitoryId}
  })
}


//---------------------------------- Sort ----------------------------------
// ตัวแปรสำหรับเก็บวิธีการจัดเรียง
const sortBy = ref('name'); // ค่าเริ่มต้นคือการจัดเรียงตามชื่อ

// ฟังก์ชันสำหรับจัดเรียงข้อมูล
const sortDormitories = (sortType) => {
  if (sortType === 'min_price') {
    dormitories.value.sort((a, b) => a.max_price - b.max_price); // เรียงจากราคาต่ำสุด
  } else if (sortType === 'max_price') {
    dormitories.value.sort((a, b) => b.min_price - a.min_price); // เรียงจากราคาสูงสุด
  } else if (sortType === 'distance') {
    dormitories.value.sort((a, b) => a.distance - b.distance); // เรียงตามระยะทาง
  } else if (sortType === 'name') {
    dormitories.value.sort((a, b) => a.dormName.localeCompare(b.dormName)); // เรียงตามชื่อ A-Z
  }
};

// การเฝ้าติดตามการเปลี่ยนแปลงของ sortBy
watch(sortBy, (newSortType) => {
  sortDormitories(newSortType);
});



//---------------------------------- Favorite ----------------------------------

// เช็คว่าหอพักอยู่ใน Favorites หรือไม่
const isFavorite = (id) => {
  return favorites.value.some((favorite) => favorite.dormId === id);
};

// เพิ่ม/ลบรายการโปรด
const handleToggleFavorite = async (id) => {

  const userId = 1;

  try {
    if (isFavorite(id)) {
      // ลบรายการโปรด
      const res = await fetch(`${API_ROOT}/favorites/dorm/${id}`, {
        method: 'DELETE',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      if (res.ok) {
        favorites.value = favorites.value.filter((fav) => fav.dormId !== id);
      } else {
        // console.error('Error removing favorite:', res.status);
      }
    } else {
      // เพิ่มรายการโปรด
      const res = await fetch(`${import.meta.env.VITE_API_ROOT}/favorites`, {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
         },
        body: JSON.stringify({ userId, dormId: id }),
      });
      if (res.ok) {
        favorites.value.push({ userId, dormId: id });
      } else {
        // console.error('Error adding favorite:', res.status);
      }
    }
  } catch (error) {
    // console.error('Error toggling favorite:', error);
  }
};


//---------------------------------- Search & Filter ----------------------------------

const searchInput = ref('')
const selectTypes = ref('')


// ตัวแปรที่เก็บค่าที่ผู้ใช้กรอก
const minPrice = ref(0);  // ราคาต่ำสุดเริ่มต้น
const maxPrice = ref(20000);  // ราคาสูงสุดเริ่มต้น

const selectedDistance = ref(0) // เก็บระยะทางที่ผู้ใช้เลือก

const filteredDormitories = computed(() => {
  // หากไม่มี searchInput ก็ให้แสดง dormitories ทั้งหมด
  if (!searchInput.value) {
    return dormitories.value.filter(dorm => {
      // กรองตามช่วงราคา
      const inPriceRange = dorm.min_price >= minPrice.value && dorm.max_price <= maxPrice.value;

      // กรองตามประเภทหอพัก
      const typeMatches = dorm.type === selectTypes.value || selectTypes.value === '';

      // กรองตามระยะทาง
      let inDistanceRange = false;
      switch (selectedDistance.value) {
        case '1': // น้อยกว่า 1 กม.
          inDistanceRange = dorm.distance < 1;
          break;
        case '2': // 1 -> 2 กม.
          inDistanceRange = dorm.distance >= 1 && dorm.distance < 2;
          break;
        case '3': // 2 -> 3 กม.
          inDistanceRange = dorm.distance >= 2 && dorm.distance < 3;
          break;
        case '4': // 3 -> 4 กม.
          inDistanceRange = dorm.distance >= 3 && dorm.distance < 4;
          break;
        case '5': // 4 -> 5 กม.
          inDistanceRange = dorm.distance >= 4 && dorm.distance < 5;
          break;
        case '6': // 5 กม. ขึ้นไป
          inDistanceRange = dorm.distance >= 5;
          break;
        case '0': // ไม่จำกัด
        default:
          inDistanceRange = true;
          break;
      }

      return inPriceRange && typeMatches && inDistanceRange;
    });
  }

  // หากมี searchInput กรองตามชื่อหอพักหรือที่อยู่
  const searchTerm = searchInput.value.toLowerCase();
  return dormitories.value.filter(dorm => {
    // กรองตามช่วงราคา
    const inPriceRange = dorm.min_price >= minPrice.value && dorm.max_price <= maxPrice.value;

    // กรองตามประเภทหอพัก
    const typeMatches = dorm.type === selectTypes.value || selectTypes.value === '';

    // กรองตามระยะทาง
    let inDistanceRange = false;
    switch (selectedDistance.value) {
      case '1': // น้อยกว่า 1 กม.
        inDistanceRange = dorm.distance < 1;
        break;
      case '2': // 1 -> 2 กม.
        inDistanceRange = dorm.distance >= 1 && dorm.distance < 2;
        break;
      case '3': // 2 -> 3 กม.
        inDistanceRange = dorm.distance >= 2 && dorm.distance < 3;
        break;
      case '4': // 3 -> 4 กม.
        inDistanceRange = dorm.distance >= 3 && dorm.distance < 4;
        break;
      case '5': // 4 -> 5 กม.
        inDistanceRange = dorm.distance >= 4 && dorm.distance < 5;
        break;
      case '6': // 5 กม. ขึ้นไป
        inDistanceRange = dorm.distance >= 5;
        break;
      case '0': // ไม่จำกัด
      default:
        inDistanceRange = true;
        break;
    }

    // กรองตามชื่อหอพักหรือที่อยู่
    const nameMatches = dorm.dormName.toLowerCase().includes(searchTerm);
    const addressMatches =
      dorm.address.street.toLowerCase().includes(searchTerm) ||
      dorm.address.subdistrict.toLowerCase().includes(searchTerm) ||
      dorm.address.district.toLowerCase().includes(searchTerm) ||
      dorm.address.province.toLowerCase().includes(searchTerm);

    return inPriceRange && typeMatches && inDistanceRange && (nameMatches || addressMatches);
  });
});

</script>

<template>

    <div class="w-full h-full flex flex-col items-center">


       <!--------------------------- Search Button & Filter + Price, Type, Distance -------------------------------------->
      <div class="flex flex-col items-center mt-5 w-3/4">

         <!--------------------------- Search Button & Filter -------------------------------------->
        <div class="flex flex-row items-stretch space-x-2 w-full pb-4">
            <SearchComponent v-model="searchInput"/>
          <div class="w-40">
            <SortComponent :dormitories="dormitories" />
          </div>    
        </div>

        <!--------------------------- Price, Type, Distance -------------------------------------->
        <div class="flex flex-col lg:flex-row items-stretch gap-6 w-full pb-4">

          <!-- ราคา (ชิดซ้าย) -->
          <div class="flex flex-col space-y-4 flex-1 min-w-[250px] border border-gray-300 rounded-lg shadow-md p-4">
            <h2 class="text-lg font-semibold text-gray-800 text-center">ราคา</h2>
            <div class="flex flex-row justify-between space-x-4">
              <div class="flex flex-col space-y-2 w-1/2">
                <div class="flex items-center space-x-2">
                  <label for="minPrice" class="text-sm font-medium text-gray-600">ราคาเริ่มต้น:</label>
                  <input
                    type="number"
                    v-model="minPrice"
                    min="0"
                    max="20000"
                    step="100"
                    class="w-full sm:w-24 text-sm border border-gray-300 rounded-md p-1 text-center"
                  />
                </div>
                <input
                  id="minPrice"
                  type="range"
                  v-model="minPrice"
                  min="0"
                  max="20000"
                  step="100"
                  class="w-full rounded-lg bg-gray-200 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div class="flex flex-col space-y-2 w-1/2">
                <div class="flex items-center space-x-2">
                  <label for="maxPrice" class="text-sm font-medium text-gray-600">ราคาสูงสุด:</label>
                  <input
                    type="number"
                    v-model="maxPrice"
                    min="0"
                    max="20000"
                    step="100"
                    class="w-24 text-sm border border-gray-300 rounded-md p-1 text-center"
                  />
                </div>
                <input
                  id="maxPrice"
                  type="range"
                  v-model="maxPrice"
                  min="0"
                  max="20000"
                  step="100"
                  class="w-full rounded-lg bg-gray-200 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
            </div>
          </div>


          <!-- ประเภทหอพัก (กึ่งกลาง) -->
          <div class="flex flex-col space-y-4 flex-1 min-w-[250px] border border-gray-300 rounded-lg shadow-md p-4">
            <h2 class="text-lg font-semibold mb-4 text-gray-800">ประเภทหอพัก</h2>
            <div class="flex flex-row justify-around mt-3">
              <label class="flex items-center space-x-2">
                <input
                  v-model="selectTypes"
                  name="default-radio"
                  type="radio"
                  value=""
                  class="form-radio w-5 h-5 text-blue-500 focus:ring-blue-500"
                />
                <span class="text-lg">ทั้งหมด</span>
              </label>
              <label class="flex items-center space-x-2">
                <input
                  v-model="selectTypes"
                  name="default-radio"
                  type="radio"
                  value="m"
                  class="form-radio w-5 h-5 text-blue-500 focus:ring-blue-500"
                />
                <span class="text-lg">ชาย</span>
              </label>
              <label class="flex items-center space-x-2">
                <input
                  v-model="selectTypes"
                  name="default-radio"
                  type="radio"
                  value="f"
                  class="form-radio w-5 h-5 text-blue-500 focus:ring-blue-500"
                />
                <span class="text-lg">หญิง</span>
              </label>
              <label class="flex items-center space-x-2">
                <input
                  v-model="selectTypes"
                  name="default-radio"
                  type="radio"
                  value="all"
                  class="form-radio w-5 h-5 text-blue-500 focus:ring-blue-500"
                />
                <span class="text-lg">รวม</span>
              </label>
            </div>
          </div>

          <!-- ระยะทาง (ชิดขวา) -->
          <div class="flex flex-col space-y-4 flex-1 min-w-[250px] border border-gray-300 rounded-lg shadow-md p-4">
            <h2 class="text-lg font-semibold text-gray-800">ระยะทาง</h2>
            <select
              id="distanceSelect"
              v-model="selectedDistance"
              class="text-lg block w-full p-3 mt-2 border border-gray-300 rounded-lg bg-white focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
            >
              <option value="0">ไม่จำกัด</option>
              <option value="1">น้อยกว่า 1 กม.</option>
              <option value="2">1 -> 2 กม.</option>
              <option value="3">2 -> 3 กม.</option>
              <option value="4">3 -> 4 กม.</option>
              <option value="5">4 -> 5 กม.</option>
              <option value="6">5 กม. ขึ้นไป</option>
            </select>
          </div>
        </div>



      <!-- items (กึ่งกลาง) -->
      <div class="grid grid-cols-1 sm:grid-cols-1 md:grid-cols-2 lg:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-3 gap-4 w-full">
          <div
            v-if="filteredDormitories !== null && filteredDormitories.length !== 0"
            v-for="dorm in filteredDormitories"
            :key="dorm.dormId"
            class="rounded-lg border-2 shadow-lg p-3 bg-white flex flex-col justify-between h-full min-h-[480px]"
          >
            <div class="w-full h-48 md:h-64 flex justify-center items-center">
              <div
                class="w-full h-full bg-cover bg-center rounded-lg"
                :style="{ backgroundImage: `url(${dorm.image[0] || '/images/no_image.jpg'})` }"
                alt="Dormitory Image"
              ></div>
            </div>

            <div class="flex flex-col w-full p-3 flex-1">
  <!-- ส่วนบน: ชื่อ + ปุ่ม favorite -->
  <div class="flex justify-between items-center">
    <h1
      @click="showDetail(dorm.dormId)"
      class="dormname cursor-pointer font-semibold text-base sm:text-lg"
      style="line-height: 1.6;"
    >
      {{ dorm.dormName }}
    </h1>
    <div v-if="userRole !== 'guest'">
      <button
        @click="handleToggleFavorite(dorm.dormId)"
        class="p-2 rounded-full border border-gray-300 hover:bg-red-100 transition"
      >
        <span :class="isFavorite(dorm.dormId) ? 'text-red-500' : 'text-gray-500'">
          {{ isFavorite(dorm.dormId) ? '❤️' : '🤍' }}
        </span>
      </button>
    </div>
  </div>

  <!-- ส่วนกลาง: รายละเอียดอื่นๆ -->
  <div class="flex flex-col flex-grow mt-2">
    <h2 class="text-green-600 text-lg">
      {{ formatPrice(dorm.min_price) }} - {{ formatPrice(dorm.max_price) }} บาท/เดือน
    </h2>
    <h2>ระยะทาง: <span>{{ dorm.distance }} กม.</span></h2>
    <h2>
      ประเภทหอพัก:
      <span v-if="dorm.type === 'all'">รวม</span>
      <span v-else-if="dorm.type === 'f'">หญิง</span>
      <span v-else-if="dorm.type === 'm'">ชาย</span>
      <span v-else>{{ dorm.type }}</span>
    </h2>

    <!-- ที่อยู่ -->
    <p class="text-sm text-gray-600 line-clamp-2">
      ที่อยู่: {{ dorm.address.street }}, {{ dorm.address.subdistrict }}, {{ dorm.address.district }},
      {{ dorm.address.province }} {{ dorm.address.postalCode }}
    </p>

    <div class="flex flex-row space-x-2 mt-5">
      <img class="w-6" src="/star.png" alt="">
      <h2>{{ dorm.rating.totalScore < 1 ? 'ยังไม่มีคะแนน' : dorm.rating.totalScore }}</h2>
    </div>
  </div>

  <!-- ปุ่มเปรียบเทียบ -->
  <div class="pt-5 w-full flex justify-center mt-auto">
    <BorderButton @click="addDormToCompare(dorm.dormId)" context="เพิ่มลงในรายการเปรียบเทียบ" />
  </div>
</div>

          </div>
        </div>


          <div v-if="dormitories.length === 0" class="text-2xl text-red-600 text-center">No Dormitory</div>
          

      </div>



      <!-- แสดงปุ่มการเปรียบเทียบ -->
      <div v-if="compareItems.length > 0" class="fixed bottom-4 right-4">
        <CompareButton/>
      </div>
</div>


  

    
 

  
</template>

<style scoped>

  input[type="radio"] {
    appearance: none; /* ลบสไตล์ดีฟอลต์ของเบราว์เซอร์ */
    width: 1rem;
    height: 1rem;
    border: 2px solid #ccc; /* กำหนดเส้นขอบของวงกลม */
    border-radius: 50%; /* ทำให้เป็นวงกลม */
    outline: none;
    transition: background-color 0.2s ease, border-color 0.2s ease;
    cursor: pointer;
  }

  input[type="radio"]:checked {
    background-color: black; /* สีภายในวงกลมเมื่อถูกเลือก */
    border-color: black; /* เปลี่ยนสีเส้นขอบเมื่อถูกเลือก */
  }

.items{
  display: flex;
  flex-direction: row;
}


/* ภาพพื้นหลังด้านบน */
.background img {
  width: 100%;
  height: 1000px; 
  object-fit: cover; 
}

.item h1 {
  font-size: 1.5rem; /* ขนาดเริ่มต้นสำหรับหน้าจอขนาดเล็ก */
  color: #F4845F;
}

.item h2 {
  font-size: 1rem;
  color: rgb(0, 0, 0);
}

.item h2 span {
  font-size: 0.9rem;
  color: #5d5d5d;
}

.item h3 span {
  font-size: 0.9rem;
  color: #5d5d5d;
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


hr{
  width: 250px;
}



input[type="range"]::-webkit-slider-runnable-track {
  width: 100%;
  height: 8px;
  border-radius: 5px;
  background: #ddd;
}

input[type="range"]::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #000000;
  cursor: pointer;
  transition: background 0.3s ease;
  /* เลื่อน thumb ขึ้นไปข้างบนเล็กน้อย */
  transform: translateY(-6px); /* ย้าย thumb ขึ้นจาก track */
}

input[type="range"]:hover::-webkit-slider-thumb {
  background: #3d3d3d;
}

input[type="range"]:focus {
  outline: none;
}


.dormname {
  font-size: 1.8rem; /* ขนาดเริ่มต้น */
  color: #F4845F;    /* สีเริ่มต้น */
  transition: transform 0.3s ease, color 0.3s ease; /* เพิ่มการเคลื่อนไหวอย่างนุ่มนวล */
}

.dormname:hover {
  color: #c45830; /* เปลี่ยนสีเมื่อ hover */
  transform: scale(1.02); /* ขยายขนาดข้อความเมื่อ hover */
}

/* ไอคอน*/
.icons img:hover  {
  transform: scale(1.02); /* ขยายขนาดเล็กน้อยเมื่อ hover */
  transition: transform 0.3s ease; /* เพิ่มการขยายขนาดอย่างนุ่มนวล */
}


</style>

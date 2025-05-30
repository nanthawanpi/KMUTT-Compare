<script setup>
import { ref, onMounted } from 'vue'
import router from '@/router';
import { getDormitoryById } from '@/composables/GetDormitories/getDormitoryById'; // นำเข้าฟังก์ชัน composable
import { useRoute } from 'vue-router'
import { useAuthorize } from '@/stores/authorize';
import { storeToRefs } from 'pinia';

const user = useAuthorize();
const { userId } = storeToRefs(user);

const API_ROOT = import.meta.env.VITE_API_ROOT
const { params } = useRoute()
const dormitoryId = params.id
const oldDormitory = ref({})

//จัดการตัวแปรแผนที่
const houseNumber1 = ref('');
const houseNumber2 = ref('');


//ฟิลในแผนที่
const searchQuery = ref('');
const dormNumber = ref('');
const street = ref('');
const subDistrict = ref('');
const district = ref('');
const province = ref('');
const postalCode = ref('');
const distance = ref()


//รายละเอียด
const dormName = ref('');
const status = ref('empty'); // เก็บค่าสถานะหอพัก (ว่าง, ไม่ว่าง)
const roomCount = ref(0);
const type = ref('all'); // เก็บค่าประเภทหอพัก (รวม, หญิง, ชาย)
const size = ref();
const min_price = ref();
const max_price = ref();
const isEditMode = ref(false);  // ใช้เพื่อระบุว่าอยู่ในโหมดเพิ่มหรือลบ


// --------------------------------- Map ---------------------------------
let map;
let userSelectedMarker = null;
let autocomplete;
const directionsService = new google.maps.DirectionsService();
const directionsRenderer = new google.maps.DirectionsRenderer();

onMounted(async () => {
  initMap();

  if (dormitoryId) {
    isEditMode.value = true;
    try {
      // ดึงข้อมูลหอพักตาม id และรอผลลัพธ์
      oldDormitory.value = await getDormitoryById(dormitoryId);

      // console.log(oldDormitory.value); // แสดงผลข้อมูลที่ได้จาก API

      // นำข้อมูลจาก oldDormitory มาใส่ในตัวแปรที่เกี่ยวข้อง
      dormName.value = oldDormitory.value.dormName || '';
      status.value = oldDormitory.value.status || 'empty';
      roomCount.value = oldDormitory.value.roomCount || 0;
      type.value = oldDormitory.value.type || 'all';
      size.value = oldDormitory.value.size || 0;
      min_price.value = oldDormitory.value.min_price || 0;
      max_price.value = oldDormitory.value.max_price || 0;
      distance.value = oldDormitory.value.distance || 0;

      // สำหรับที่อยู่
      if (oldDormitory.value.address) {
        dormNumber.value = oldDormitory.value.address.dormNumber || '';
        street.value = oldDormitory.value.address.street || '';
        subDistrict.value = oldDormitory.value.address.subdistrict || '';
        district.value = oldDormitory.value.address.district || '';
        province.value = oldDormitory.value.address.province || '';
        postalCode.value = oldDormitory.value.address.postalCode || '';
      }

      // กำหนดสิ่งอำนวยความสะดวก
      insideAmenities.value = oldDormitory.value.room_facility || [];
      outsideAmenities.value = oldDormitory.value.building_facility || [];

      // กำหนดรูปภาพที่เลือก
      selectedImages.value = oldDormitory.value.image || [];
      // console.log('ข้อมูลรูปที่ดึงมาจากเดิม' + selectedImages.value)

      
    } catch (error) {
      // console.error('Error fetching dormitory:', error);
    }
  }
});

function initMap() {
  const kmuttLocation = { lat: 13.651309958082942, lng: 100.49645730815111 };
  map = new google.maps.Map(document.getElementById('map'), {
    zoom: 14,
    center: kmuttLocation,
  });

  directionsRenderer.setMap(map);

  const input = document.getElementById('place-input');
  autocomplete = new google.maps.places.Autocomplete(input);
  autocomplete.setComponentRestrictions({ country: ['th'] });

  autocomplete.addListener('place_changed', () => {
    const place = autocomplete.getPlace();
    if (place.geometry) {
      map.setCenter(place.geometry.location);
      map.setZoom(15);

      if (userSelectedMarker) {
        userSelectedMarker.setPosition(place.geometry.location);
      } else {
        userSelectedMarker = new google.maps.Marker({
          position: place.geometry.location,
          map: map,
          title: place.name,
        });
      }

      calculateDistance(place.geometry.location);

      if (place.address_components) {
        houseNumber1.value = '';
        houseNumber2.value = '';
        street.value = '';
        subDistrict.value = '';
        district.value = '';
        province.value = '';
        postalCode.value = '';
        dormNumber.value = '';

        // console.log(place.address_components)

        place.address_components.forEach(component => {
          const componentType = component.types[0];
          switch (componentType) {
            case 'subpremise':
              houseNumber1.value = component.long_name;
              break;
            case 'street_number':
              houseNumber2.value = component.long_name;
              break;
            case 'route':
              street.value = component.long_name;
              break;
            case 'sublocality_level_2':
              subDistrict.value = component.long_name;
              break;
            case 'sublocality_level_1':
              district.value = component.long_name;
              break;
            case 'administrative_area_level_1':
              province.value = component.long_name;
              break;
            case 'postal_code':
              postalCode.value = component.long_name;
              break;
            default:
              break;
          }
        });

        if (houseNumber1.value && houseNumber2.value) {
          dormNumber.value = `${houseNumber1.value}/${houseNumber2.value}`;
        } else {
          dormNumber.value = houseNumber1.value || houseNumber2.value;
        }

        searchQuery.value = place.name;

      } else {
        alert("No details available for input: '" + place.name + "'");
      }
    }
  });
}

function calculateDistance(destination) {
  const kmuttLocation = { lat: 13.6513, lng: 100.4964 };
  const request = {
    origin: destination,
    destination: kmuttLocation,
    travelMode: google.maps.TravelMode.DRIVING,
  };

  directionsService.route(request, (result, status) => {
    if (status === google.maps.DirectionsStatus.OK) {
      directionsRenderer.setDirections(result);
      const route = result.routes[0];
      const distanceText = route.legs[0].distance.text;
      
      // เอาเฉพาะตัวเลขจากระยะทาง
      distance.value = distanceText.split(' ')[0];  // แยกตัวเลขออกจากหน่วย เช่น "5.2" จาก "5.2 km"

    } else {
      alert('ไม่สามารถคำนวณระยะทางได้: ' + status);
    }
  });
}


// --------------------------------- Manage Images ---------------------------------

const selectedImages = ref([]);
// const MAX_FILE_SIZE = 2097152;
// การจัดการเมื่อเลือกไฟล์จากเครื่อง
const handleFiles = async (event) => {
  const files = event.target.files;
  // console.log(files)
  for (let i = 0; i < files.length; i++) {
    const file = files[i];
    // if(file.size > MAX_FILE_SIZE){
    //   alert('ไฟล์มีขนาดใหญ่เกินไป กรุณาอัปโหลดไฟล์ที่มีขนาดน้อยกว่า 2 MB')
    //   continue; // ข้ามไฟล์นี้และไปที่ไฟล์ถัดไป
    // }
    const imageUrl = URL.createObjectURL(file);  // สร้าง URL สำหรับไฟล์ภาพที่เลือก
    selectedImages.value.push({ file, imageUrl, uploadedImageUrl: null });  // เก็บทั้ง file, imageUrl และ placeholder สำหรับ uploadedImageUrl

    // อัปโหลดภาพไปยังเซิร์ฟเวอร์
    await uploadImage(file);
  }
};

// อัปโหลดภาพไปยังเซิร์ฟเวอร์
const uploadImage = async (file) => {
  const formData = new FormData();
  formData.append('files', file);

  try {
    const response = await fetch(`${API_ROOT}/images/upload`, {
      method: 'POST',
      body: formData,
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });

    if (!response.ok) {
      const index = selectedImages.value.findIndex(img => img.file === file);
      if (index !== -1) {
        selectedImages.value.splice(index, 1); // ลบไฟล์ออกจาก selectedImages.value
      }

      if (response.status === 413) {
        alert("ไฟล์ใหญ่เกินไป! กรุณาอัปโหลดไฟล์ที่เล็กลง");
      } else if (response.status === 400) {
        alert("ประเภทไฟล์ไม่ถูกต้อง อนุญาตเฉพาะไฟล์ JPEG, GIF, PNG, BMP และ WEBP เท่านั้น");
      }

      throw new Error('Upload failed');
    }

    const data = await response.json(); // เซิร์ฟเวอร์ส่งข้อมูลที่เกี่ยวข้องกับไฟล์
    data.forEach(image => {
      const uploadedImageUrl = image.fileUrl; // รับ URL ของภาพที่อัปโหลดจาก response
      const index = selectedImages.value.findIndex(img => img.file === file);
      if (index !== -1) {
        selectedImages.value[index] = uploadedImageUrl; // อัปเดต uploadedImageUrl
      }
    });

  } catch (error) {
    console.error('Error uploading image:', error);
  }
};


// ลบภาพจากเซิร์ฟเวอร์
const deleteImage = async (imageUrl) => {
  // console.log(imageUrl)
  const imageId = imageUrl.split('/').pop();  // ดึง ID จาก URL ของภาพที่อัปโหลดหรือที่เลือก
  // console.log(imageId)
  

  try {
    const response = await fetch(`${API_ROOT}/images/${imageId}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });

    if (!response.ok) {
      throw new Error('Delete failed');
    }

    // ลบภาพที่ถูกลบออกจาก selectedImages
    
    // console.log('ลบสำเร็จ');
    // console.log(selectedImages.value)
    selectedImages.value = selectedImages.value.filter(img => img !== imageUrl);

  } catch (error) {
    // console.error('Error deleting image:', error);
  }
};



// --------------------------------- Manage Facilities ---------------------------------
const insideAmenities = ref([]);
const outsideAmenities = ref([]);

const newInsideAmenity = ref('');
const newOutsideAmenity = ref('');

const insideSuggestions = ref(["เตียงเดี่ยว","เตียงคู่","เครื่องปรับอากาศ", "เฟอร์นิเจอร์-ตู้", "เครื่องทำน้ำอุ่น", "พัดลม", "ทีวี/TV", "ตู้เย็น","โซฟา","โต๊ะ - เก้าอี้ทำงาน","เตาปรุงอาหาร","โทรศัพท์สายตรง","อินเทอร์เน็ตไร้สาย (WIFI) ในห้อง","เคเบิลทีวี / ดาวเทียม"]);
const filteredInsideSuggestions = ref([]);

const outsideSuggestions = ref(["ที่จอดรถ","ที่จอดรถมอเตอร์ไซด์/จักรยาน","ลิฟต์", "โรงยิม / ฟิตเนส", "สระว่ายน้ำ", "สนามเด็กเล่น","มีระบบรักษาความปลอดภัย (keycard)","มีระบบรักษาความปลอดภัย (สแกนลายนิ้วมือ)","กล้องวงจรปิด (CCTV)","รปภ.","ร้านขายอาหาร","ร้านค้า สะดวกซื้อ","ร้านซัก-รีด / มีบริการเครื่องซักผ้า","ร้านทำผม-เสริมสวย","สถานี charge รถไฟฟ้า"]);
const filteredOutsideSuggestions = ref([]);

// Inside Amenities
const filterInsideSuggestions = () => {
  filteredInsideSuggestions.value = insideSuggestions.value.filter((suggestion) =>
    suggestion.includes(newInsideAmenity.value.trim())
  );
};

const selectInsideSuggestion = (suggestion) => {
  newInsideAmenity.value = suggestion;
  filteredInsideSuggestions.value = [];
};

const addInsideAmenity = () => {
  if (newInsideAmenity.value.trim() !== '') {
    insideAmenities.value.push(newInsideAmenity.value.trim());
    newInsideAmenity.value = '';
    filteredInsideSuggestions.value = [];
  }
};

const removeInsideAmenity = (index) => {
  insideAmenities.value.splice(index, 1);
};

// Outside Amenities
const filterOutsideSuggestions = () => {
  filteredOutsideSuggestions.value = outsideSuggestions.value.filter((suggestion) =>
    suggestion.includes(newOutsideAmenity.value.trim())
  );
};

const selectOutsideSuggestion = (suggestion) => {
  newOutsideAmenity.value = suggestion;
  filteredOutsideSuggestions.value = [];
};

const addOutsideAmenity = () => {
  if (newOutsideAmenity.value.trim() !== '') {
    outsideAmenities.value.push(newOutsideAmenity.value.trim());
    newOutsideAmenity.value = '';
    filteredOutsideSuggestions.value = [];
  }
};

const removeOutsideAmenity = (index) => {
  outsideAmenities.value.splice(index, 1);
};

const dormitoryData = {
    userId: userId.value,
    dormName: '',
    status: '',
    address: '',
    roomCount: 0,
    type: '',
    size: 0,
    min_price: 0,
    max_price: 0,
    created_at: null,
    updated_at: null,
    distance: 0,
    image: '',
    building_facility: '',
    room_facility: '',
  };


const nameError = ref('')
const distanceError = ref('')
const minPriceError = ref('')
const maxPriceError = ref('')
const dormNumberError = ref('')
const streetError = ref('')
const subDistrictError = ref('')
const districtError = ref('')
const provinceError = ref('')
const postalCodeError = ref('')
const roomCountError = ref('')
const sizeError = ref('')
const insideAmenitiesError = ref('')
const outsideAmenitiesError = ref('')
  // --------------------------------- Add/Edit ---------------------------------

import ConfirmModal from '@/components/modals/ConfirmModal.vue';
import SuccessModal from '@/components/modals/SuccessModal.vue';

const isModalVisible = ref(false);
const modalContext = ref('');

const isModalSuccessVisible = ref(false)
const modalProps = ref({ title: '', message: '' });

// Function to open the modal with the correct context
const showModal = (context) => {
  modalContext.value = context;
  isModalVisible.value = true;
};

// Function to close the modal
const closeModal = () => {
  isModalVisible.value = false;
};  

// Function to handle the submit logic
const handleSubmit = async () => {
  // Reset error messages before validation
  nameError.value = '';
  distanceError.value = '';
  minPriceError.value = '';
  maxPriceError.value = '';
  roomCountError.value = '';
  dormNumberError.value = '';
  streetError.value = '';
  subDistrictError.value = '';
  districtError.value = '';
  provinceError.value = '';
  postalCodeError.value = '';
  sizeError.value = '';
  insideAmenitiesError.value = '';
  outsideAmenitiesError.value = '';

  // Validation flag
  let isValid = true;

  // Validate form data
  if (!dormName.value) {
    nameError.value = 'กรุณากรอกชื่อที่พัก';
    isValid = false;
  }
  if (!distance.value || isNaN(distance.value)) {
    distanceError.value = 'กรุณากรอกระยะทางเป็นตัวเลข';
    isValid = false;
  }
  if (!min_price.value || isNaN(min_price.value)) {
    minPriceError.value = 'กรุณากรอกราคาเริ่มต้นเป็นตัวเลข';
    isValid = false;
  }
  if (!max_price.value || isNaN(max_price.value)) {
    maxPriceError.value = 'กรุณากรอกราคาสูงสุดเป็นตัวเลข';
    isValid = false;
  }
  if (roomCount.value <= 0) {
    roomCountError.value = 'กรุณาระบุจำนวนห้องพักที่เหลือให้เช่า';
    isValid = false;
  }
  if (!dormNumber.value) {
    dormNumberError.value = 'กรุณากรอกเลขที่หอพัก';
    isValid = false;
  }
  if (!street.value) {
    streetError.value = 'กรุณากรอกถนน ซอย';
    isValid = false;
  }
  if (!subDistrict.value) {
    subDistrictError.value = 'กรุณากรอกตำบล/แขวง';
    isValid = false;
  }
  if (!district.value) {
    districtError.value = 'กรุณากรอกอำเภอ/เขต';
    isValid = false;
  }
  if (!province.value) {
    provinceError.value = 'กรุณากรอกจังหวัด';
    isValid = false;
  }
  if (!postalCode.value) {
    postalCodeError.value = 'กรุณากรอกรหัสไปรษณีย์';
    isValid = false;
} else if (!/^\d{1,5}$/.test(postalCode.value)) {
    postalCodeError.value = 'กรุณากรอกรหัสไปรษณีย์เป็นตัวเลข ไม่เกิน 5 ตัว';
    isValid = false;
}
  if (!size.value || isNaN(size.value)) {
    sizeError.value = 'กรุณากรอกขนาดห้องเป็นตัวเลข';
    isValid = false;
  }
  if (insideAmenities.value.length === 0) {
    insideAmenitiesError.value = 'กรุณาเพิ่มเฟอร์นิเจอร์ภายในห้องพัก';
    isValid = false;
  }
  if (outsideAmenities.value.length === 0) {
    outsideAmenitiesError.value = 'กรุณาเพิ่มเฟอร์นิเจอร์ภายนอกอาคาร';
    isValid = false;
  }

  // If validation passes, open the confirmation modal
  if (isValid) {
    showModal(isEditMode.value ? 'update' : 'add');
  }
};

// ฟังก์ชันที่จัดการกับการยืนยันใน modal
const handleConfirmAction = async (context) => {
  // console.log('Confirm action for:', context);

  dormitoryData.value = {
    userId: userId.value,
    dormName: dormName.value,
    status: status.value,
    address: {
      dormNumber: dormNumber.value,
      street: street.value,
      subdistrict: subDistrict.value,
      district: district.value,
      province: province.value,
      postalCode: postalCode.value,
    },
    roomCount: parseInt(roomCount.value),
    type: type.value,
    size: parseFloat(size.value),
    min_price: parseFloat(min_price.value),
    max_price: parseFloat(max_price.value),
    created_at: new Date().toISOString(),
    updated_at: new Date().toISOString(),
    distance: parseFloat(distance.value),
    image: selectedImages.value, // Image URLs
    room_facility: insideAmenities.value,
    building_facility: outsideAmenities.value,
  };

  try {
    let res;
    const token = localStorage.getItem('token');
    if (context === 'update') {  // Handle update case
      res = await fetch(`${API_ROOT}/dormitories/${dormitoryId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + token
        },
        body: JSON.stringify(dormitoryData.value),
      });
    } else if (context === 'add') {  // Handle add case
      res = await fetch(`${API_ROOT}/dormitories`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + token
        },
        body: JSON.stringify(dormitoryData.value),
      });
    }

    if (res.ok) {
      const responseJson = await res.json();
      const successMessage = context === 'update' 
        ? { title: 'อัปเดตหอพักสำเร็จ', message: 'ข้อมูลหอพักถูกอัปเดตเรียบร้อยแล้ว' }
        : { title: 'เพิ่มหอพักสำเร็จ', message: 'ข้อมูลหอพักถูกเพิ่มเรียบร้อยแล้ว' };

      // ส่งข้อมูลไปยัง modal
      isModalSuccessVisible.value = true; // ให้แสดง modal
      modalProps.value = successMessage; // ส่งข้อมูล props ไปที่ modal
      // console.log(modalContext.value)

    } else {
      const errorResponse = await res.json();
      alert(`ไม่สามารถ ${context === 'update' ? 'อัปเดต' : 'เพิ่ม'} หอพักได้: ${errorResponse.message || 'โปรดลองใหม่อีกครั้ง'}`);
    }
  } catch (error) {
    // console.error('เกิดข้อผิดพลาดในการส่งข้อมูล:', error);
    // alert('เกิดข้อผิดพลาดในการส่งข้อมูล');
  }

  // Close the modal after action
  closeModal();
};
 
</script>



<template>
  <div class="w-full h-full flex justify-center mt-24">

    <div class="w-8/12 space-y-5">

      <h1>{{ isEditMode ? 'แก้ไขหอพัก' : 'ลงประกาศที่พัก' }}</h1>

      <!-- ชื่อ -->
      <div class="flex flex-row items-center w-full">
        <div class="w-28 text-lg font-medium">
          <p>ชื่อที่อยู่ที่พัก:<span class="text-red-500">*</span></p>
        </div>

        <div class="relative w-full">
          <input 
            type="text" 
            class="rounded-md ml-2 w-full text-lg" 
            placeholder="ระบุชื่อที่พัก.." 
            v-model="dormName"
            :class="{'border-red-500': !dormName}"
            required 
          />
          <p v-if="nameError" class="pl-2 text-red-500 text-sm mt-1">{{ nameError }}</p>
        </div>
        
      </div>


     <!-- Map -->

     <div class="flex flex-row items-center w-full mb-5">
      <div class="w-28"><p class="text-lg font-medium">ที่อยู่ที่พัก:</p></div>
      <input
      id="place-input"
      type="text"
      placeholder="ค้นหาสถานที่"
      class="rounded-md ml-2 w-full text-lg"
      v-model="searchQuery"
    />

    </div>
    
    <div id="map" style="width: 100%; height: 300px;"></div>

    <div class="flex flex-col mt-5">
      <h2>กรอกข้อมูลที่อยู่</h2>

      <div class="grid gap-6 mb-6 md:grid-cols-2 mt-5">
  <div>
    <label for="dormNumber" class="block mb-2 text-lg text-gray-900 dark:text-white">
      เลขที่
      <span class="text-red-500">*</span>
    </label>
    <input 
      v-model="dormNumber" 
      type="text" 
      id="dormNumber" 
      class="bg-gray-50 border border-gray-300 text-gray-900 text-lg rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" 
      placeholder="เลขที่" 
      required 
    />
    <p v-if="dormNumberError" class="text-red-500 text-sm">{{ dormNumberError }}</p>
  </div>

  <div>
    <label for="street" class="block mb-2 text-lg text-gray-900 dark:text-white">
      ถนน ซอย
      <span class="text-red-500">*</span>
    </label>
    <input 
      v-model="street" 
      type="text" 
      id="street" 
      class="bg-gray-50 border border-gray-300 text-gray-900 text-lg rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" 
      placeholder="ถนน ซอย" 
      required 
    />
    <p v-if="streetError" class="text-red-500 text-sm">{{ streetError }}</p>
  </div>

  <div>
    <label for="subDistrict" class="block mb-2 text-lg text-gray-900 dark:text-white">
      ตำบล/แขวง
      <span class="text-red-500">*</span>
    </label>
    <input 
      v-model="subDistrict" 
      type="text" 
      id="subDistrict" 
      class="bg-gray-50 border border-gray-300 text-gray-900 text-lg rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" 
      placeholder="ตำบล/แขวง" 
      required 
    />
    <p v-if="subDistrictError" class="text-red-500 text-sm">{{ subDistrictError }}</p>
  </div>

  <div>
    <label for="district" class="block mb-2 text-lg text-gray-900 dark:text-white">
      อำเภอ/เขต
      <span class="text-red-500">*</span>
    </label>
    <input 
      v-model="district" 
      type="tel" 
      id="district" 
      class="bg-gray-50 border border-gray-300 text-gray-900 text-lg rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" 
      placeholder="อำเภอ/เขต" 
      pattern="[0-9]{3}-[0-9]{2}-[0-9]{3}" 
      required 
    />
    <p v-if="districtError" class="text-red-500 text-sm">{{ districtError }}</p>
  </div>

  <div>
    <label for="province" class="block mb-2 text-lg text-gray-900 dark:text-white">
      จังหวัด
      <span class="text-red-500">*</span>
    </label>
    <input 
      v-model="province" 
      type="url" 
      id="province" 
      class="bg-gray-50 border border-gray-300 text-gray-900 text-lg rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" 
      placeholder="จังหวัด" 
      required 
    />
    <p v-if="provinceError" class="text-red-500 text-sm">{{ provinceError }}</p>
  </div>

  <div>
    <label for="postalCode" class="block mb-2 text-lg text-gray-900 dark:text-white">
      รหัสไปรษณีย์
      <span class="text-red-500">*</span>
    </label>
    <input 
      v-model="postalCode" 
      type="url" 
      id="postalCode" 
      class="bg-gray-50 border border-gray-300 text-gray-900 text-lg rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" 
      placeholder="รหัสไปรษณีย์" 
      required 
    />
    <p v-if="postalCodeError" class="text-red-500 text-sm">{{ postalCodeError }}</p>
  </div>

  <div>
    <label for="distance" class="block mb-2 text-lg text-gray-900 dark:text-white">
      ระยะทาง (กม.)
      <span class="text-red-500">*</span>
    </label>
    <input 
      v-model="distance" 
      type="text" 
      id="distance" 
      class="bg-gray-50 border border-gray-300 text-gray-900 text-lg rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" 
      placeholder="ระยะทาง" 
    />
    <p v-if="distanceError" class="text-red-500 text-sm">{{ distanceError }}</p>
  </div>
</div>


    </div>


      <h2>รายละเอียดที่พัก</h2>

            <!-- ประเภท -->
            <div class="flex flex-row space-x-5">
        <div class="w-32">
            <label class="block mb-2 text-lg text-gray-900 dark:text-white ">ประเภทหอพัก</label>
            <select id="type" v-model="type" class="cursor-pointer bg-gray-50 border border-gray-300 text-gray-900 text-lg rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">
              <!-- ใช้ค่า value เป็นภาษาอังกฤษ -->
              <option selected value="all">รวม</option>
              <option value="m">ชาย</option>
              <option value="f">หญิง</option>
            </select>
        </div>
        <div class="w-32">
          <label class="block mb-2 text-lg text-gray-900 dark:text-white ">สถานะหอพัก</label>
          <select id="status" v-model="status" class="cursor-pointer bg-gray-50 border border-gray-300 text-gray-900 text-lg rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">
            <!-- ค่า value เป็นภาษาอังกฤษ, แสดงค่าภาษาไทย -->
            <option selected value="empty">ว่าง</option>
            <option value="full">ไม่ว่าง</option>
          </select>
        </div>
      </div>


      <!-- ราคา -->
      <div class="flex flex-row space-x-8">
        <div class="flex flex-col">
          <div class="flex flex-row items-center">
            <p for="min-price" class="w-44 text-lg">ราคาเริ่มต้น: (ต่อเดือน)<span class="text-red-500">*</span></p>
            <input v-model="min_price" type="text" id="min_price" class="ml-2 border border-gray-300 text-gray-900 text-lg rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-42 p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" placeholder="โปรดระบุเป็นตัวเลข" required />
          </div>
          <p v-if="minPriceError" class="pl-44 ml-2 text-red-500 text-sm">{{ minPriceError }}</p>
        </div>
        <div class="flex flex-col">
        <div class="flex flex-row items-center">
          <p for="max-price" class="w-44 text-lg">ราคาสูงสุด: (ต่อเดือน)<span class="text-red-500">*</span></p>
          <input v-model="max_price" type="text" id="max_price" class="border border-gray-300 text-gray-900 text-lg rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-42 p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" placeholder="โปรดระบุเป็นตัวเลข" required />
        </div>
        <p v-if="maxPriceError" class="pl-44 ml-2 text-red-500 text-sm">{{ maxPriceError }}</p>
      </div>
      </div>

      <div class="flex flex-row space-x-5">
        <div class="flex flex-col">
              <!-- จำนวนห้องพักที่เหลือให้เช่า -->
              <div class="flex flex-row items-center space-x-3">
                <p for="room" class="w-42 text-lg">จำนวนห้องพักที่เหลือให้เช่า:<span class="text-red-500">*</span></p>
                  <div class="py-2 px-3 inline-block bg-white border border-gray-200 rounded-lg dark:bg-neutral-900 dark:border-neutral-700">
                    <div class="flex items-center gap-x-1.5">
                      <button @click="roomCount > 0 ? roomCount-- : null" :disabled="roomCount==0" type="button" class="size-6 inline-flex justify-center items-center gap-x-2 text-sm font-medium rounded-md border border-gray-200 bg-white text-gray-800 shadow-sm hover:bg-gray-50 focus:outline-none focus:bg-gray-50 disabled:opacity-50 disabled:pointer-events-none dark:bg-neutral-900 dark:border-neutral-700 dark:text-white dark:hover:bg-neutral-800 dark:focus:bg-neutral-800">
                        <svg class="shrink-0 size-3.5" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                          <path d="M5 12h14"></path>
                        </svg>
                      </button>
                      <input class="p-0 w-6 bg-transparent border-0 text-gray-800 text-center focus:ring-0 [&::-webkit-inner-spin-button]:appearance-none [&::-webkit-outer-spin-button]:appearance-none dark:text-white" v-model="roomCount">
                      <button @click="roomCount++" type="button" class="size-6 inline-flex justify-center items-center gap-x-2 text-sm font-medium rounded-md border border-gray-200 bg-white text-gray-800 shadow-sm hover:bg-gray-50 focus:outline-none focus:bg-gray-50 disabled:opacity-50 disabled:pointer-events-none dark:bg-neutral-900 dark:border-neutral-700 dark:text-white dark:hover:bg-neutral-800 dark:focus:bg-neutral-800">
                        <svg class="shrink-0 size-3.5" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                          <path d="M5 12h14"></path>
                          <path d="M12 5v14"></path>
                        </svg>
                      </button>
                    </div>
                  </div>
              </div>
              <p v-if="roomCountError" class="text-red-500 text-sm mt-1">{{ roomCountError }}</p>
            </div>
          </div>

        <div class="flex flex-row items-center">
          <div class="flex flex-col">
            <div class="flex flex-row items-center">
              <p for="max-price" class="w-36 text-lg">ขนาดห้อง: (ตร.ม.)<span class="text-red-500">*</span></p>
              <input v-model="size" type="text" id="size" class="border border-gray-300 text-gray-900 text-lg rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-42 p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" placeholder="โปรดระบุเป็นตัวเลข" required />
            </div>
            <p v-if="sizeError" class="pl-36 ml-2 text-red-500 text-sm">{{ sizeError }}</p>
          </div>
        </div>



     <!-- สิ่งอำนวยความสะดวกต่างๆ -->
     <div class="grid gap-6 md:grid-cols-2 pt-5">
      <!-- สิ่งอำนวยความสะดวกภายในห้องพัก -->
      <div class="room space-y-5">
        <h3>สิ่งอำนวยความสะดวกภายในห้องพัก<span class="text-red-500">*</span></h3>
        <div class="flex flex-row w-full">
        <div class="relative w-full">
          <input 
            v-model="newInsideAmenity" 
            type="text" 
            class="border border-gray-300 text-lg rounded-lg p-2 w-full"
            placeholder="เพิ่มสิ่งอำนวยความสะดวกภายในห้อง"
            @input="filterInsideSuggestions"
          />
          <p v-if="insideAmenitiesError" class="ml-2 text-red-500 text-sm">{{ insideAmenitiesError }}</p>
          <!-- Suggestion List -->
          <ul 
            v-if="filteredInsideSuggestions.length" 
            class="absolute bg-white border border-gray-300 w-2/3 mt-1 rounded-lg shadow-lg z-10"
          >
            <li 
              v-for="(suggestion, index) in filteredInsideSuggestions" 
              :key="index" 
              class="p-2 hover:bg-gray-100 cursor-pointer"
              @click="selectInsideSuggestion(suggestion)"
            >
              {{ suggestion }}
            </li>
          </ul>
        </div>
        <button 
          @click="addInsideAmenity" 
          class="ml-2 bg-blue-500 text-white rounded-lg p-2 px-8"
        >
          เพิ่ม
        </button>
      </div>
        <ul class="border">
          <li 
            v-for="(amenity, index) in insideAmenities" 
            :key="index" 
            class="text-lg flex justify-between p-2"
          >
            <span>{{ amenity }}</span>
            <button 
              @click="removeInsideAmenity(index)" 
              class="text-red-500 hover:text-red-700"
            >
              ลบ
            </button>
          </li>
        </ul>
      </div>

      <!-- สิ่งอำนวยความสะดวกภายนอกอาคาร -->
      <div class="outside space-y-5">
        <h3>สิ่งอำนวยความสะดวกภายนอกอาคาร<span class="text-red-500">*</span></h3>
        <div class="flex flex-row w-full">
        
          <div class="relative w-full">
          <input 
            v-model="newOutsideAmenity" 
            type="text" 
            class="border border-gray-300 text-lg rounded-lg p-2 w-full"
            placeholder="เพิ่มสิ่งอำนวยความสะดวกภายนอกอาคาร"
            @input="filterOutsideSuggestions"
          />
          <p v-if="outsideAmenitiesError" class="ml-2 text-red-500 text-sm">{{ outsideAmenitiesError }}</p>
          <!-- Suggestion List -->
          <ul 
            v-if="filteredOutsideSuggestions.length" 
            class="absolute bg-white border border-gray-300 w-full mt-1 rounded-lg shadow-lg z-10"
          >
            <li 
              v-for="(suggestion, index) in filteredOutsideSuggestions" 
              :key="index" 
              class="p-2 hover:bg-gray-100 cursor-pointer"
              @click="selectOutsideSuggestion(suggestion)"
            >
              {{ suggestion }}
            </li>
          </ul>
        </div>

            <button 
              @click="addOutsideAmenity" 
              class="ml-2 bg-blue-500 text-white rounded-lg p-2 px-8"
            >
              เพิ่ม
            </button>

      </div>
        <ul class="border">
          <li 
            v-for="(amenity, index) in outsideAmenities" 
            :key="index" 
            class="text-lg flex justify-between p-2"
          >
            <span>{{ amenity }}</span>
            <button 
              @click="removeOutsideAmenity(index)" 
              class="text-red-500 hover:text-red-700"
            >
              ลบ
            </button>
          </li>
        </ul>
      </div>

  </div>


    <div class="pt-5">
      <h2>อัปโหลดรูปภาพ</h2>
      <div class="flex flex-col items-start">
        <label class="flex flex-col items-center cursor-pointer">
          <div class="file-input-container border-dashed border-2 border-gray-400 p-4 rounded-lg text-center">
            <span class="text-blue-600 font-semibold">คลิกเพื่อเลือกไฟล์</span>
          </div>
          <input type="file" class="hidden" multiple @change="handleFiles" />
        </label>
      </div>



      <div class="flex flex-wrap mt-4" id="imagePreview">
    <!-- แสดงตัวอย่างภาพที่เลือก -->
    <div v-for="(image, index) in selectedImages" :key="index" class="relative m-1">
      <img :src="image" class="w-28 h-28 object-cover" />
      <button
        @click="deleteImage(image)"
        class="absolute top-0 right-0 bg-red-500 text-white p-1 rounded-full"
      >
        ลบ
      </button>
    </div>
  </div>


      
    </div>

    <div class="w-full flex items-center justify-end space-x-2 h-20">
      <button @click="handleSubmit" class="btn bg-orange-500 text-white w-28 hover:bg-orange-600">
          {{ isEditMode ? 'อัปเดตหอพัก' : 'เพิ่มหอพัก' }}
        </button>
        <button @click="router.push('/')" class="btn w-28">ยกเลิก</button>
    </div>
    </div>
  </div>
       <!-- Confirmation Modal -->
       <ConfirmModal
        :isVisible="isModalVisible"
        :context="modalContext"
        @close="closeModal"
        @confirm="handleConfirmAction"
      />

          <!-- เมื่อ isModalVisible เป็น true จะทำให้แสดง SuccessModal -->
      <SuccessModal
        v-if="isModalSuccessVisible"
        :title="modalProps.title"
        :message="modalProps.message"
        @close="isModalSuccessVisible = false" 
        :context="modalContext"
      />
</template>

<style scoped>
.file-input-container {
  transition: border 0.2s ease;
}

.file-input-container:hover {
  border-color: blue;
}
h1{
  font-size: 2rem;
  font-weight:500
}

h2{
  font-size: 1.5rem;
  font-weight:500
}

h3{
  font-size: 1.25rem;
  font-weight:500
}

#imagePreview img {
  border-radius: 5px; /* ทำให้มุมรูปภาพกลม */
  transition: transform 0.2s;
}

#imagePreview img:hover {
  transform: scale(1.05); /* ขยายเมื่อชี้เมาส์ */
}
</style>

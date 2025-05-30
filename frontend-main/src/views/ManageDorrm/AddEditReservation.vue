<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import SuccessModal from '@/components/modals/SuccessModal.vue';
import { formatLocalDateTime } from '@/composables/formatDate';
import { validateEmail, validateName, validatePhone } from '@/composables/Validate/validateUserData';
import { fetchUserProfile } from '@/composables/GetUsers/getUserProfile';

const API_ROOT = import.meta.env.VITE_API_ROOT;
const { params } = useRoute();


const isModalSuccessVisible = ref(false);
const modalProps = ref({ title: '', message: '' });
const isLoading = ref(false);

const maxLength = 200;


onMounted(async () => {
  if(params.action === 'add'){
    form.value.dormId = params.id
  }

  if (params.action === 'edit' && form.value) {
    await fetchFormData(params.id);
  }

  // ตรวจสอบว่า params.action คือ 'cancel' และ form มีข้อมูล
  if (params.action === 'cancel' && form.value) {
    await fetchFormData(params.id);
    form.value.description = 'ยกเลิกการจอง' 
    submitForm()
  }
  
  const profileData = await fetchUserProfile();
  // console.log(profileData.name)
  if (profileData) {
    form.value.name = profileData.name;
    form.value.email = profileData.email;
    form.value.phone = profileData.phone;
  }
});

// ฟอร์มข้อมูล
const form = ref({
  name: '',
  email: '',
  phone: '',
  date_in: '',
  date_out: '',
  description: '',
});

// **Validation Rules**
const errors = ref({
  name: '',
  email: '',
  phone: '',
  date_in: '',
  date_out: '',
  description: '',
});

// ฟังก์ชันการเลื่อนหน้าจอไปยังช่องที่มี error
const scrollToError = (field) => {
  const element = document.getElementById(field);
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }
};


// คำนวณจำนวนตัวอักษรที่เหลือ
const remainingChars = computed(() => maxLength - form.value.description.length);

// ฟังก์ชันแปลงวันที่ให้เป็น ISO
const formatDateTime = (date) => {
  const dateTime = new Date(date);
  return dateTime.toISOString();
};

// ฟังก์ชันตรวจสอบ validation ของแต่ละฟิลด์
const validateField = (field) => {
  switch (field) {
    case 'name':
      if (!form.value.name.trim()) {
        errors.value.name = 'กรุณากรอกชื่อ-นามสกุล';
      } else if (form.value.name.length > 50) {
        errors.value.name = 'ชื่อ-นามสกุลต้องไม่เกิน 50 ตัวอักษร';
      } else if (!validateName(form.value.name)) {
        errors.value.name = 'ชื่อ-นามสกุลต้องเป็นตัวอักษรเท่านั้น';
      } else {
        errors.value.name = '';
      }
      break;

    case 'email':
      if (!form.value.email.trim()) {
        errors.value.email = 'กรุณากรอกอีเมล';
      } else if (!validateEmail(form.value.email)) {
        errors.value.email = 'รูปแบบอีเมลไม่ถูกต้อง';
      } else {
        errors.value.email = '';
      }
      break;

    case 'phone':
      if (!form.value.phone.trim()) {
        errors.value.phone = 'กรุณากรอกเบอร์โทร';
      } else if (!validatePhone(form.value.phone)) {
        errors.value.phone = 'เบอร์โทรต้องเป็นตัวเลขและไม่เกิน 10 ตัว';
      } else {
        errors.value.phone = '';
      }
      break;

    case 'date_in':
      if (!form.value.date_in.trim()) {
        errors.value.date_in = 'กรุณาระบุวันที่เข้าพัก';
      } else {
        const today = new Date();
        const dateIn = new Date(form.value.date_in);

        if (dateIn < today) {
          errors.value.date_in = 'วันที่เข้าพักต้องเป็นวันนี้หรือหลังจากนี้';
        } else {
          errors.value.date_in = '';
        }
      }
      break;

    case 'date_out':
      if (!form.value.date_out.trim()) {
        errors.value.date_out = 'กรุณาระบุวันที่ออก';
      } else {
        const dateIn = new Date(form.value.date_in);
        const dateOut = new Date(form.value.date_out);
        if (dateOut < dateIn) {
          errors.value.date_out = 'วันที่ออกต้องมากกว่าหรือเท่ากับวันที่เข้าพัก';
        } else {
          errors.value.date_out = '';
        }
      }
      break;
      
    case 'description':
      if (form.value.description.length > maxLength) {
        errors.value.description = `จำนวนตัวอักษรเกิน ${maxLength} ตัว`;
      } else {
        errors.value.description = '';
      }
      break;
  }
};

// ฟังก์ชันดึงข้อมูลสำหรับแก้ไข
const fetchFormData = async (f) => {
  // ตรวจสอบว่า params.id มีค่าหรือไม่
  if (!params.id) {
    // console.error('ID ไม่ถูกต้อง');
    return;
  }

  isLoading.value = true;  // เริ่มโหลดข้อมูล
  try {
    // ดึงข้อมูลจาก API
    const response = await fetch(`${API_ROOT}/user/sent-form/${params.id}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}` // ใช้ token ใน localStorage
      }
    });
    
    // ถ้าการตอบกลับจาก API สำเร็จ
    if (response.ok) {
      const data = await response.json();
      
      // ตั้งค่าฟอร์มจากข้อมูลที่ดึงมา
      form.value = {
        ...data,
        date_in: formatLocalDateTime(data.date_in),  // แปลงวันที่
        date_out: formatLocalDateTime(data.date_out)  // แปลงวันที่
      };
    }
    
    // ถ้า response มีสถานะ 401 (Unauthorized) ให้รีเฟรช token
    else if (response.status === 401) {
      await getNewToken();  // รีเฟรช token
      await fetchFormData(f);  // พยายามดึงข้อมูลใหม่หลังจากรีเฟรช token
    } else {
      // console.error('ไม่สามารถดึงข้อมูลฟอร์มได้');
    }
  } catch (error) {
    // console.error('เกิดข้อผิดพลาด:', error);
  } finally {
    isLoading.value = false;  // สิ้นสุดการโหลดข้อมูล
  }
};






const submitForm = async () => {
  if (params.action !== 'cancel') {
    // Validate all fields before submitting
    validateField('name');
    validateField('email');
    validateField('phone');
    validateField('date_in');
    validateField('date_out');
    validateField('description');

    // หากมีข้อผิดพลาดในฟิลด์ใดๆ ให้เลื่อนไปที่ฟิลด์นั้น
    for (const field in errors.value) {
      if (errors.value[field]) {
        scrollToError(field);
        return; // หยุดการส่งฟอร์มทันทีเมื่อพบข้อผิดพลาด
      }
    }
  }
  // แปลง date_in และ date_out ให้เป็น ISO ก่อนส่ง
  form.value.date_in = formatDateTime(form.value.date_in);
  form.value.date_out = formatDateTime(form.value.date_out);
  form.value.form_date = new Date().toISOString
  isLoading.value = true;

  try {
    const url = `${API_ROOT}/user/sent-form${params.action === 'edit' || params.action === 'cancel' ? `/${params.id}` : ''}`;
    const method = params.action === 'edit' || params.action === 'cancel' ? 'PUT' : 'POST';
    
    const response = await fetch(url, {
      method,
      headers: { 
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}` 
      },
      body: JSON.stringify(form.value),
    });

    if (response.ok) {
        isModalSuccessVisible.value = true;
        modalProps.value = { title: 'ส่งฟอร์มสำเร็จ', message: form.value.description === 'ยกเลิกการจอง' 
        ? 'ยกเลิกการจองเรียบร้อยแล้ว' 
        : 'ส่งฟอร์มจองหอพักเรียบร้อยแล้ว' 
      };

      if (params.action === 'edit') {
        isModalSuccessVisible.value = true;
        modalProps.value = { title: 'ส่งฟอร์มสำเร็จ', message: 'อัปเดตฟอร์มจองหอพักเรียบร้อยแล้ว'};
      }
      
    } else {
      // console.error('ไม่สามารถส่งข้อมูลได้');
    }
  } catch (error) {
    // console.error('เกิดข้อผิดพลาด:', error);
  } finally {
    isLoading.value = false;
    
  }
};



</script>

<template>
  <div class="min-h-screen flex justify-center items-center bg-gray-100 p-4">
    <div class="w-full max-w-xl bg-white p-8 rounded-lg shadow-lg">
      <h2 class="text-2xl font-semibold text-center mb-6">ฟอร์มการจองหอพัก</h2>

      <form @submit.prevent="submitForm">
        <!-- ชื่อ-นามสกุล -->
        <div class="mb-4" id="name">
          <label class="block text-sm font-medium text-gray-700">ชื่อ-นามสกุล</label>
          <input 
            type="text" 
            v-model="form.name" 
            @input="validateField('name')" 
            class="input input-bordered w-full" 
            placeholder="สมชาย ใจดี" />
          <p class="text-red-500 text-sm" v-if="errors.name">{{ errors.name }}</p>
        </div>

        <!-- อีเมล -->
        <div class="mb-4" id="email">
          <label class="block text-sm font-medium text-gray-700">อีเมล</label>
          <input 
            type="email" 
            v-model="form.email" 
            @input="validateField('email')" 
            class="input input-bordered w-full" 
            placeholder="example@example.com" />
          <p class="text-red-500 text-sm" v-if="errors.email">{{ errors.email }}</p>
        </div>

        <!-- เบอร์โทร -->
        <div class="mb-4" id="phone">
          <label class="block text-sm font-medium text-gray-700">เบอร์โทร</label>
          <input 
            type="text" 
            v-model="form.phone" 
            @input="validateField('phone')" 
            class="input input-bordered w-full" 
            placeholder="0812345678" />
          <p class="text-red-500 text-sm" v-if="errors.phone">{{ errors.phone }}</p>
        </div>

        <!-- วันที่และเวลาเข้าพัก -->
        <div class="mb-4" id="date_in">
          <label class="block text-sm font-medium text-gray-700">วันที่และเวลาที่เข้าพัก</label>
          <input 
            type="datetime-local" 
            v-model="form.date_in" 
            @input="validateField('date_in')" 
            class="input input-bordered w-full" 
            placeholder="เลือกวันที่และเวลาที่ต้องการเข้าพัก" />
          <p class="text-red-500 text-sm" v-if="errors.date_in">{{ errors.date_in }}</p>
        </div>

        <!-- วันที่และเวลาออก -->
        <div class="mb-4" id="date_out">
          <label class="block text-sm font-medium text-gray-700">วันที่และเวลาที่ออก</label>
          <input 
            type="datetime-local" 
            v-model="form.date_out" 
            @input="validateField('date_out')" 
            class="input input-bordered w-full" 
            placeholder="เลือกวันที่และเวลาที่ต้องการออกจากหอพัก" />
          <p class="text-red-500 text-sm" v-if="errors.date_out">{{ errors.date_out }}</p>
        </div>

        <!-- รายละเอียดเพิ่มเติม -->
        <div class="mb-4" id="description">
          <label class="block text-sm font-medium text-gray-700">รายละเอียดเพิ่มเติม</label>
          <textarea v-model="form.description" @input="validateField('description')" class="input input-bordered w-full h-32" placeholder="เช่น คำขอพิเศษ หรือความต้องการเฉพาะอื่นๆ"></textarea>
          <p class="text-right text-sm text-gray-500">{{ remainingChars }} ตัวอักษรเหลือ</p>
          <p class="text-red-500 text-sm" v-if="errors.description">{{ errors.description }}</p>
        </div>

        <!-- ปุ่มยืนยัน -->
        <button type="submit" class="btn btn-primary w-full">ยืนยันการจอง</button>
      </form>
    </div>
  </div>

  <!-- Spinner Modal -->
  <div v-if="isLoading" class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
    <div class="bg-white p-6 rounded-lg flex flex-col items-center">
      <span class="loading loading-spinner loading-lg"></span>
      <p class="mt-4 text-gray-700">กำลังส่งข้อมูล...</p>
    </div>
  </div>

  <!-- Success Modal -->
  <SuccessModal v-if="isModalSuccessVisible" :title="modalProps.title" :message="modalProps.message" context="reservation" @close="isModalSuccessVisible = false" />
</template>

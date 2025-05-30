<script setup>
import Sidebar from '@/components/Sidebar.vue';
import { clearAllToken, clearToken } from '@/composables/Authentication/clearToken';
import { getNewToken } from '@/composables/Authentication/getNewToken';
import { fetchUserProfile } from '@/composables/GetUsers/getUserProfile';
import { validatePhone, validateEmail, validateName, validatePassword, validateUsername } from '@/composables/Validate/validateUserData';
import { useUIStore } from '@/stores/uiStore';
import { ref, onMounted, watch } from 'vue';

const uiStore = useUIStore()

const API_ROOT = import.meta.env.VITE_API_ROOT;

const loading = ref(false);
const loadingPass = ref(false);
const successMessage = ref('');
const successMessagePass = ref('');
const errorMessage = ref('');  // เพิ่ม errorMessage
const errorMessagePass = ref('');

// ข้อมูลผู้ใช้
const username = ref('');
const name = ref('');
const email = ref('');
const phone = ref('');
const oldPassword = ref('');
const newPassword = ref('');
const confirmPassword = ref('');


// ดึงข้อมูลผู้ใช้เมื่อคอมโพเนนต์ถูกโหลด
onMounted(async () => {
  const profileData = await fetchUserProfile();
  if (profileData) {
    username.value = profileData.username;
    name.value = profileData.name;
    email.value = profileData.email;
    phone.value = profileData.phone;
  }
});

// ข้อความผิดพลาด
const errors = ref({
  username: '',
  name: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: ''
});

// ฟังก์ชันตรวจสอบรหัสผ่าน
const validatePasswordField = () => {
  if (!newPassword.value || !confirmPassword.value) {
    return false; // ให้ return false ถ้ายังไม่ได้กรอกรหัสผ่าน
  }
  if (newPassword.value !== confirmPassword.value) {
    errors.value.confirmPassword = 'รหัสผ่านใหม่และยืนยันรหัสผ่านไม่ตรงกัน';
    return false;
  }
  if (!validatePassword(newPassword.value)) {
    errors.value.confirmPassword = 'กรุณากรอกรหัสผ่าน รหัสผ่านต้องมีตัวอักษรเล็ก, ตัวอักษรใหญ่, ตัวเลข, อักขระพิเศษ และความยาวระหว่าง 8 ถึง 20 ตัว โดยไม่สามารถมีช่องว่างได้';
    return false;
  }
  errors.value.confirmPassword = '';
  return true;
};


watch([newPassword, confirmPassword], () => {
  validatePasswordField(); // เรียกตรวจสอบรหัสผ่านทุกครั้งที่ newPassword หรือ confirmPassword เปลี่ยนค่า
});


watch([name], () => {
  validateData(); 
});

watch([email], () => {
  validateData(); 
});

watch([phone], () => {
  validateData(); 
});



const validateData = () => {
  errors.value.username = validateUsername(username.value) ? '' : 'กรุณากรอกชื่อผู้ใช้ (ไม่เกิน 50 ตัวอักษร)';
  errors.value.name = validateName(name.value) ? '' : 'กรุณากรอกชื่อ-นามสกุล โดยจะต้องเป็นตัวอักษรและไม่เกิน 50 ตัวอักษร';
  errors.value.email = validateEmail(email.value) ? '' : 'กรุณากรอกอีเมล โดยอีเมลจะต้องมีรูปแบบที่ถูกต้อง (เช่น example@domain.com)';
  errors.value.phone = validatePhone(phone.value) ? '' : 'กรุณากรอกเบอร์โทรศัพท์ให้ถูกต้อง (10 หลัก)';

  return !errors.value.username && !errors.value.name && !errors.value.email && !errors.value.phone;
};



const updateProfile = async () => {
  if (!validateData()) return;

  loading.value = true;
  successMessage.value = '';
  errorMessage.value = ''; // เคลียร์ข้อความ error ก่อนการอัปเดต

  const oldUsername = localStorage.getItem('username'); // ดึง username เดิมมาเช็ค

  const formData = {
    username: username.value,
    name: name.value,
    email: email.value,
    phone: phone.value,
  };

  try {
    const response = await fetch(`${API_ROOT}/user/me`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': "Bearer " + localStorage.getItem('token'),
      },
      body: JSON.stringify(formData),
    });

    if (!response.ok) {
      const errorData = await response.json();

      if (errorData.message === "Username already exists") {
        errorMessage.value = '❌ username นี้มีผู้ใช้แล้ว'; 
      } 
      else if (errorData.message === "Email already exists") {
        errorMessage.value = '❌ email นี้มีผู้ใช้แล้ว';  
      } 
      else {
        errorMessage.value = '❌ บันทึกข้อมูลไม่สำเร็จ';
      }

      throw new Error('ไม่สามารถอัปเดตข้อมูลได้');
    }

    loading.value = false;
    successMessage.value = '✅ บันทึกข้อมูลสำเร็จ';

    // ✅ เช็คว่ามีการเปลี่ยน username ไหม
    if (oldUsername && oldUsername !== username.value) {
      setTimeout(() => {
        clearAllToken()
        alert('กรุณาเข้าสู่ระบบใหม่อีกครั้ง')
        uiStore.openLoginPopup()
      }, 1500); // รอให้ข้อความ success แสดงก่อน logout
    }

    setTimeout(() => {
      successMessage.value = '';
    }, 3000);

  } catch (error) {
    loading.value = false;
  }
};






// ฟังก์ชันอัปเดตข้อมูลรหัสผ่าน
const changePassword = async () => {
  if (!validatePasswordField()) return;

  loadingPass.value = true;
  successMessagePass.value = '';
  errorMessagePass.value = ''; // เคลียร์ข้อความ error ก่อนการอัปเดต

  try {
    const response = await fetch(`${API_ROOT}/change-password`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': "Bearer " + localStorage.getItem('token')
      },
      body: JSON.stringify({
        oldPassword: oldPassword.value, 
        newPassword: newPassword.value
      }),
    });

    if (!response.ok) {
      errorMessagePass.value = '❌ รหัสผ่านเดิมไม่ถูกต้อง';
      throw new Error('ไม่สามารถอัปเดตข้อมูลได้');
    }

    loadingPass.value = false;
    successMessagePass.value = '✅ เปลี่ยนรหัสผ่านสำเร็จ';

    setTimeout(() => {
      successMessagePass.value = '';
    }, 3000);

  } catch (error) {
    loadingPass.value = false;
    // ข้อความ error จะถูกแสดงจากข้อผิดพลาดใน try-catch
  }
};

</script>

<template>
  <div class="flex flex-row w-full justify-center p-20">
    <Sidebar class="hidden lg:block w-64" />
    <div class="pl-0 lg:pl-2 w-full lg:w-1/2 h-full">
      <div class="p-4 border-2 border-gray-200 border-dashed rounded-lg dark:border-gray-700">
        <div class="flex flex-col space-y-6">
          
          <!-- แสดงข้อความ Success -->
          <div v-if="successMessage" class="bg-green-200 text-green-800 p-3 rounded-lg mb-4">
            {{ successMessage }}
          </div>

          <!-- แสดงแถบ Loading ถ้าอยู่ในสถานะกำลังโหลด -->
          <div v-if="loading" class="bg-blue-200 text-blue-800 p-3 rounded-lg mb-4">
            กำลังบันทึก...
          </div>

          <!-- แสดงข้อความ Error ถ้าไม่สำเร็จ -->
          <div v-if="errorMessage" class="bg-red-200 text-red-800 p-3 rounded-lg mb-4">
            {{ errorMessage }}
          </div>

          <h1 class="text-3xl">แก้ไขข้อมูลสมาชิก</h1>

          <div class="flex flex-row items-center">
            <p for="name_surname" class="w-32 text-lg">ชื่อผู้ใช้:</p>
            <input v-model="username" type="text" class="input-style" placeholder="username" />
          </div>
          <span v-if="errors.username" class="pl-32 text-red-500 text-sm mt-1">{{ errors.username }}</span>

          <div class="flex flex-row items-center">
            <p for="name_surname" class="w-32 text-lg">ชื่อ-นามสกุล:</p>
            <input v-model="name" type="text" class="input-style" placeholder="ชื่อ-นามสกุล" />
          </div>
          <span v-if="errors.name" class="pl-32 text-red-500 text-sm mt-1">{{ errors.name }}</span>

          <div class="flex flex-row items-center">
            <p for="email" class="w-32 text-lg">อีเมล:</p>
            <input v-model="email" type="email" class="input-style" placeholder="อีเมล" />
          </div>
          <span v-if="errors.email" class="pl-32 text-red-500 text-sm mt-1">{{ errors.email }}</span>

          <div class="flex flex-row items-center">
            <p for="telephone" class="w-32 text-lg">เบอร์โทรศัพท์:</p>
            <input v-model="phone" type="tel" class="input-style" placeholder="หมายเลขโทรศัพท์" />
          </div>
          <span v-if="errors.phone" class="pl-32 text-red-500 text-sm mt-1">{{ errors.phone }}</span>

          <button @click="updateProfile" :disabled="!validateData()" class="ml-0 sm:ml-32 btn bg-orange-500 text-white hover:bg-orange-600 w-full sm:w-9/12 mx-auto sm:mx-0">บันทึกข้อมูล</button>

          <!-- แสดงข้อความ Success -->
          <div v-if="successMessagePass" class="bg-green-200 text-green-800 p-3 rounded-lg mb-4">
            {{ successMessagePass }}
          </div>

          <!-- แสดงแถบ Loading ถ้าอยู่ในสถานะกำลังโหลด -->
          <div v-if="loading" class="bg-blue-200 text-blue-800 p-3 rounded-lg mb-4">
            กำลังบันทึก...
          </div>

          <!-- แสดงข้อความ Error ถ้าไม่สำเร็จ -->
          <div v-if="errorMessagePass" class="bg-red-200 text-red-800 p-3 rounded-lg mb-4">
            {{ errorMessagePass }}
          </div>
          
          <h1 class="text-3xl">เปลี่ยนรหัสผ่าน</h1>
          <div class="flex flex-row items-center relative">
            <p for="oldPassword" class="w-32 text-lg">รหัสผ่านปัจจุบัน:</p>
            <input 
              type="password" 
              v-model="oldPassword" 
              class="input-style pr-10" 
              placeholder="รหัสผ่านปัจจุบัน" 
            />
          </div>

          <div class="flex flex-row items-center relative">
            <p for="newPassword" class="w-32 text-lg">รหัสผ่านใหม่:</p>
            <input 
              type="password" 
              v-model="newPassword" 
              class="input-style pr-10" 
              placeholder="รหัสผ่านใหม่" 
            />
          </div>

          <div class="flex flex-row items-center relative">
            <p for="confirmPassword" class="w-32 text-lg">ยืนยันรหัสผ่าน:</p>
            <input 
              type='password'
              v-model="confirmPassword" 
              class="input-style pr-10" 
              placeholder="ยืนยันรหัสผ่าน" 
            />
          </div>

          <span v-if="errors.confirmPassword" class="text-red-500 text-sm mt-1">{{ errors.confirmPassword }}</span>

          <button @click="changePassword" class="ml-0 sm:ml-32 btn bg-orange-500 text-white hover:bg-orange-600 w-full sm:w-9/12 mx-auto sm:mx-0">เปลี่ยนรหัสผ่าน</button>
        </div>
      </div>
    </div>
  </div>
</template>


<style>
.input-style {
  width: 75%;
  padding: 10px 14px;
  border: 2px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  outline: none;
  transition: all 0.3s ease-in-out;
  background-color: #fff;
}

.input-style:focus {
  border-color: #f97316; /* สีส้ม */
  box-shadow: 0 0 5px rgba(249, 115, 22, 0.5);
}

.input-style::placeholder {
  color: #a0aec0;
  font-style: italic;
}

.input-style:disabled {
  background-color: #f7f7f7;
  cursor: not-allowed;
}


.toggle-btn {
  position: absolute;
  right: 50px;
  cursor: pointer;
  background: none;
  border: none;
  font-size: 18px;
  color: #888;
}

.toggle-btn:hover {
  color: #f97316;
}

</style>

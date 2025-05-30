<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { getUserById } from '@/composables/GetUsers/getUserById';
import { validateEmail, validateName, validatePassword, validatePhone, validateUsername } from '@/composables/Validate/validateUserData';
import SuccessModal from '@/components/modals/SuccessModal.vue';

const isModalSuccessVisible = ref(false);
const modalProps = ref({ title: '', message: '' });

const API_ROOT = import.meta.env.VITE_API_ROOT;
const { params } = useRoute();
const router = useRouter();

const userData = ref({
  username: '',
  name: '',
  email: '',
  phone: '',
  role: 'user',
  password: ''
});

const mode = ref('add');
const passwordVisible = ref(false); // สำหรับแสดง/ซ่อนรหัสผ่าน
const errorMessages = ref({
  username: '',
  email: '',
  password: '',
  phone: ''
});


const generatePassword = () => {
  const length = 12;
  const uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  const lowercase = "abcdefghijklmnopqrstuvwxyz";
  const numbers = "0123456789";
  const specialChars = "!@#$%^&*()_-+=<>?";
  const allChars = lowercase + uppercase + numbers + specialChars;

  let password = "";
  // Ensure at least one character from each category
  password += uppercase.charAt(Math.floor(Math.random() * uppercase.length)); 
  password += numbers.charAt(Math.floor(Math.random() * numbers.length)); 
  password += specialChars.charAt(Math.floor(Math.random() * specialChars.length));

  // Fill up the rest of the password
  for (let i = password.length; i < length; i++) {
    password += allChars.charAt(Math.floor(Math.random() * allChars.length));
  }

  // Shuffle password to avoid predictable order
  password = password.split("").sort(() => 0.5 - Math.random()).join("");

  // Ensure the password meets validation criteria
  if (validatePassword(password)) {
    userData.value.password = password;
  } else {
    // In case the generated password doesn't meet the validation, regenerate it
    generatePassword();
  }
};


// ฟังก์ชันการตรวจสอบข้อมูล
const validateData = () => {
  errorMessages.value.username = !userData.value.username || validateUsername(userData.value.username) ? '' : 'กรุณากรอกชื่อผู้ใช้ (ไม่เกิน 50 ตัวอักษร)';
  errorMessages.value.name = !userData.value.name || validateName(userData.value.name) ? '' : 'กรุณากรอกชื่อ-นามสกุล โดยจะต้องเป็นตัวอักษรและไม่เกิน 50 ตัวอักษร';
  errorMessages.value.email = !userData.value.email || validateEmail(userData.value.email) ? '' : 'กรุณากรอกอีเมล โดยอีเมลจะต้องมีรูปแบบที่ถูกต้อง (เช่น example@domain.com)';
  errorMessages.value.phone = !userData.value.phone || validatePhone(userData.value.phone) ? '' : 'กรุณากรอกเบอร์โทรศัพท์ให้ถูกต้อง (10 หลัก)';
  errorMessages.value.password = !userData.value.password || validatePassword(userData.value.password) ? '' : 'กรุณากรอกรหัสผ่าน รหัสผ่านต้องมีตัวอักษรเล็ก, ตัวอักษรใหญ่, ตัวเลข, อักขระพิเศษ และความยาวระหว่าง 8 ถึง 20 ตัว โดยไม่สามารถมีช่องว่างได้';
};

// ฟังก์ชันการบันทึกข้อมูล
const save = async () => {
  // ตรวจสอบข้อมูลก่อนบันทึก
  validateData();
  
  // ถ้ามี error ในฟอร์ม ให้ return ออกก่อน
  if (Object.values(errorMessages.value).some(error => error)) return;

  try {
    const response = mode.value === 'add'
      ? await fetch(`${API_ROOT}/admin/user`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': "Bearer " + localStorage.getItem('token')
          },
          body: JSON.stringify(userData.value),
        })
      : await fetch(`${API_ROOT}/admin/user/${params.id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': "Bearer " + localStorage.getItem('token')
          },
          body: JSON.stringify(userData.value),
        });

    if (!response.ok) {
      const errorData = await response.json();
      if (errorData.message === 'Email already exists') {
        errorMessages.value.email = 'อีเมลนี้มีผู้ใช้งานแล้ว';
      } else if (errorData.message === 'Username already exists') {
        errorMessages.value.username = 'ชื่อผู้ใช้นี้มีผู้ใช้งานแล้ว';
      } else {
        // alert(errorData.message || 'Failed to save user data');
      }
      return;
    }

    isModalSuccessVisible.value = true;
    modalProps.value = {
      title: mode.value === 'add' 
        ? 'เพิ่มบัญชีผู้ใช้สำเร็จ'
        : `แก้ไขบัญชีผู้ใช้สำเร็จ`,
      message: mode.value === 'add'
        ? 'เพิ่มผู้ใช้ใหม่เข้าสู่ระบบเรียบร้อยแล้ว'
        : `อัปเดตข้อมูลผู้ใช้ ID: ${params.id} เรียบร้อยแล้ว`
    };

  } catch (error) {
    // console.error('Error saving user:', error);
    // alert('An error occurred while saving the user data');
  }
};

onMounted(async () => {
  if (params.id) {
    mode.value = 'edit';
    userData.value = await getUserById(params.id);
  } else {
    mode.value = 'add';
  }
});

// Watch สำหรับตรวจสอบค่าทุกฟิลด์ของ userData และตรวจสอบข้อผิดพลาดแบบ realtime
watch(
  [
    () => userData.value.username,
    () => userData.value.name,
    () => userData.value.email,
    () => userData.value.phone,
    () => userData.value.password
  ],
  validateData
);
</script>

<template>
  <div class="flex items-center justify-center min-h-screen bg-gray-100">
    <div class="p-6 w-1/4 bg-white rounded-xl shadow-md space-y-6">
      <h2 class="text-2xl font-semibold text-gray-700 text-center">
        {{ mode === 'edit' ? 'Edit User' : 'Add New User' }}
      </h2>
      <div class="space-y-4">
        <!-- Username -->
        <div>
          <label for="username" class="block text-sm font-medium text-gray-600">Username</label>
          <input id="username" v-model="userData.username" placeholder="Username" class="input" :class="{'border-red-500': errorMessages.username}" />
          <span v-if="errorMessages.username" class="text-red-500 text-sm">{{ errorMessages.username }}</span>
        </div>

        <!-- Name -->
        <div>
          <label for="name" class="block text-sm font-medium text-gray-600">Name</label>
          <input id="name" v-model="userData.name" placeholder="Name" class="input" />
          <span v-if="errorMessages.name" class="text-red-500 text-sm">{{ errorMessages.name }}</span>
        </div>

        <!-- Email -->
        <div>
          <label for="email" class="block text-sm font-medium text-gray-600">Email</label>
          <input id="email" v-model="userData.email" placeholder="Email" class="input" :class="{'border-red-500': errorMessages.email}" />
          <span v-if="errorMessages.email" class="text-red-500 text-sm">{{ errorMessages.email }}</span>
        </div>

        <!-- Role -->
        <div>
          <label for="role" class="block text-sm font-medium text-gray-600">Role</label>
          <select id="role" v-model="userData.role" class="input">
            <option value="user">User</option>
            <option value="admin">Admin</option>
          </select>
        </div>

        <!-- ฟิลด์ Phone -->
        <div>
          <label for="phone" class="block text-sm font-medium text-gray-600">Phone</label>
          <input id="phone" v-model="userData.phone" placeholder="Phone" class="input" :class="{'border-red-500': errorMessages.phone}" />
          <span v-if="errorMessages.phone" class="text-red-500 text-sm">{{ errorMessages.phone }}</span>
        </div>

        <!-- ฟิลด์ Password -->
        <div v-if="mode === 'add'">
          <label for="password" class="block text-sm font-medium text-gray-600">Password</label>
          <div class="flex items-center space-x-2">
            <input 
              v-if="mode === 'add'"
              v-model="userData.password"
              :type="passwordVisible ? 'text' : 'password'"
              placeholder="Password"
              class="input"
              :class="{'border-red-500': errorMessages.password}" 
            />
            <button type="button" @click="passwordVisible = !passwordVisible" class="text-blue-500 text-sm">
              {{ passwordVisible ? 'Hide Password' : 'Show Password' }}
            </button>
          </div>
          <span v-if="errorMessages.password" class="text-red-500 text-sm">{{ errorMessages.password }}</span>

          <!-- ปุ่มสำหรับ generate password -->
          <div v-if="mode === 'add'" class="my-4">
            <button @click="generatePassword" class="btn bg-blue-500 text-white">
              Generate Password
            </button>
          </div>

        </div>
      </div>
      <div class="flex justify-end space-x-2">
        <button @click="router.back()" class="btn bg-gray-300">Cancel</button>
        <button @click="save" class="btn bg-blue-500 text-white">Save</button>
      </div>
    </div>
  </div>

  <SuccessModal v-if="isModalSuccessVisible" :title="modalProps.title" :message="modalProps.message" context="users" @close="isModalSuccessVisible = false" />
</template>

<style scoped>
.input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 5px;
}

.btn {
  padding: 8px 12px;
  border-radius: 5px;
  font-weight: bold;
  cursor: pointer;
}

.flex {
  display: flex;
}

.items-center {
  align-items: center;
}

.justify-center {
  justify-content: center;
}

.min-h-screen {
  min-height: 100vh;
}

.border-red-500 {
  border-color: red;
}

.text-red-500 {
  color: red;
}

.text-green-500 {
  color: green;
}
</style>

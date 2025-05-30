<script setup>
import { ref } from 'vue';
import Sidebar from '@/components/Sidebar.vue';
import { clearAllToken } from '@/composables/Authentication/clearToken';
import SuccessModal from '@/components/modals/SuccessModal.vue';
import router from '@/router';

const API_ROOT = import.meta.env.VITE_API_ROOT;
const isSuccessModelOpen = ref(false);

const showPopup = ref(false);
const username = ref('');
const password = ref('');
const confirm = ref('');
const agreeTerms = ref(false);

const confirmDelete = async () => {
    try {
        if (!agreeTerms.value) {
            alert("กรุณายอมรับข้อกำหนดและเงื่อนไขก่อนลบบัญชี");
            return;
        }

        if (!username.value || !password.value || confirm.value !== "delete account") {
            alert("กรุณากรอกข้อมูลให้ครบถ้วนและพิมพ์ 'delete account'");
            return;
        }

        const response = await fetch(`${API_ROOT}/user/me`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify({
                username: username.value,
                password: password.value,
                confirm: confirm.value
            })
        });

        if (!response.ok) {
            if (response.status === 403 || response.status === 401) {
                alert("ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง");
            } else if (response.status === 400) {
                alert("ยังมีหอพักอยู่ในบัญชี กรุณาจัดการหอพักก่อนลบบัญชี");
            } else {
                throw new Error('ไม่สามารถลบบัญชีได้');
            }
            return;
        }

        alert("บัญชีของคุณถูกลบเรียบร้อยแล้ว");
        clearAllToken();
        window.location.href = '/';
    } catch (error) {
        alert("เกิดข้อผิดพลาดในการลบบัญชี");
    }
};
</script>

<template>
  <div class="flex flex-col lg:flex-row w-full justify-center p-4 sm:p-10 lg:p-20">
    <Sidebar class="hidden lg:block w-64" />
    <div class="pl-0 lg:pl-2 w-full lg:w-1/2 h-full">
      <div class="p-4 border-2 border-gray-200 border-dashed rounded-lg dark:border-gray-700">
        <h1 class="font-bold text-3xl mb-6">ยินดีต้อนรับสู่หน้าสนับสนุน</h1>
        <p class="text-lg mb-4">หากคุณพบปัญหาหรือมีคำถามเกี่ยวกับการใช้งานเว็บไซต์ของเรา ทีมงานยินดีที่จะช่วยเหลือคุณ</p>

        <h2 class="text-2xl font-semibold mb-4">วิธีการติดต่อเรา</h2>
        <p class="mb-4">คุณสามารถติดต่อเราผ่านช่องทางต่างๆ ดังนี้:</p>
        <ul class="list-disc pl-6 mb-6">
          <li><span class="font-medium">อีเมล:</span> <a href="mailto:support@example.com" class="text-blue-500 underline hover:text-blue-800">jipjip.kmutt.compare@gmail.com</a></li>
          <li><span class="font-medium">เบอร์โทรศัพท์:</span> 123-456-7890 (จันทร์ - ศุกร์, 9.00 - 18.00 น.)</li>
          <li><span class="font-medium">แชทออนไลน์:</span> คุณสามารถใช้ฟีเจอร์แชทของเราผ่านทางเว็บไซต์เพื่อพูดคุยกับตัวแทนฝ่ายสนับสนุนได้ทันที</li>
        </ul>

        <h2 class="text-2xl font-semibold mb-4">คำถามที่พบบ่อย</h2>
        <p class="mb-4">หากคุณมีคำถามที่พบบ่อย, เราขอแนะนำให้คุณตรวจสอบคำถามที่พบบ่อยที่หน้า <a href="/faq" class="text-blue-500 underline hover:text-blue-800">คำถามที่พบบ่อย</a> ของเรา</p>

        <h2 class="text-2xl font-semibold mb-4">แจ้งปัญหาหรือข้อเสนอแนะ</h2>
        <p class="mb-4">หากคุณพบปัญหาในการใช้งานเว็บไซต์ หรือมีข้อเสนอแนะในการปรับปรุง, กรุณาส่งอีเมลมาที่ <a href="mailto:support@example.com" class="text-blue-500 underline hover:text-blue-800">jipjip.kmutt.compare@gmail.com</a>.</p>

        <h2 class="text-2xl font-semibold mb-4">การเป็นเจ้าของและการควบคุมบัญชี</h2>
        <p class="mb-4">การลบโปรไฟล์ หรือบัญชีของคุณ จะมีผลถาวร...</p>
        <button @click="showPopup = !showPopup">ลบบัญชี</button>
      </div>
    </div>

    <!-- Popup Confirm Delete -->
    <div v-if="showPopup" class="popup-overlay">
      <div class="popup-content">
        <h2>ยืนยันการลบบัญชี</h2>
        <p>กรุณากรอกข้อมูลเพื่อยืนยันการลบบัญชี</p>

        <div class="space-y-2 text-start">
          <input v-model="username" placeholder="Username" class="input" required />
          <input v-model="password" type="password" placeholder="Password" class="input" required />
          <p>กรุณาป้อนข้อความด้านล่างเพื่อยืนยันการลบบัญชี</p>
          <input v-model="confirm" placeholder="พิมพ์ 'delete account'" class="input" required />
        </div>

        <p class="mt-5"><strong class="text-lg">ข้อควรทราบ:</strong></p>
        <div class="flex flex-col text-start items-start">
          <ul class="text-gray-500 list-disc ml-5">
            <li>หากบัญชีของท่านมีการลงทะเบียนหอพักอยู่ ท่านจะไม่สามารถลบบัญชีได้โดยทันที ท่านจำเป็นต้องดำเนินการ <span @click="router.push({name:'dormlists'})" class="text-orange-500 font-semibold cursor-pointer hover:text-orange-600">โอนย้ายหอพักไปยังบัญชีอื่น หรือ ลบหอพัก </span>ออกจากบัญชีก่อน</li>
            <li>เมื่อบัญชีของท่านไม่มีหอพักค้างอยู่แล้ว จึงจะสามารถดำเนินการลบบัญชีได้</li>
          </ul>
        </div>

        <div class="flex flex-row items-center space-x-2 mt-5">
          <input type="checkbox" v-model="agreeTerms" id="agreeTerms" class="checkbox" />
          <label for="agreeTerms">ฉันยอมรับข้อกำหนดและเงื่อนไขในการลบบัญชี</label>
        </div>

        <div class="popup-buttons">
          <button @click="confirmDelete" class="confirm">ยืนยัน</button>
          <button @click="showPopup = false" class="cancel">ยกเลิก</button>
        </div>
      </div>
    </div>

    <div v-if="isSuccessModelOpen">
      <SuccessModal title="บัญชีของคุณได้ถูกลบเรียบร้อยแล้ว" />
    </div>
  </div>
</template>

<style scoped>
h1 {
  font-size: 2rem;
  color: #2b2b2b;
}
h2 {
  font-size: 1.5rem;
  color: #333;
}
p {
  font-size: 1rem;
  line-height: 1.6;
  color: #666;
}
span {
  font-size: 1rem;
}
ul {
  list-style-type: disc;
  margin-left: 20px;
}
button {
  background-color: #ea2300;
  border: none;
  color: white;
  padding: 10px 20px;
  font-size: 1rem;
  cursor: pointer;
  border-radius: 5px;
}
button:hover {
  background-color: #c71e01;
}

/* Popup Style */
.popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 50;
}

.popup-content {
  background: white;
  padding: 20px;
  border-radius: 10px;
  width: 90%;
  max-width: 400px;
  text-align: center;
}

.input {
  background-color: #f9fafb;
  border: 1px solid #d1d5db;
  color: #111827;
  padding: 0.5rem;
  border-radius: 0.5rem;
  width: 100%;
  font-size: 0.875rem;
}

.checkbox {
  width: 1rem;
  height: 1rem;
  border: 1px solid #d1d5db;
  border-radius: 0.25rem;
}

.popup-buttons {
  margin-top: 10px;
  display: flex;
  justify-content: space-around;
}

/* Confirm Button */
.confirm {
  background-color: #ea2300;
  color: white;
  padding: 8px 16px;
  border: none;
  cursor: pointer;
  border-radius: 5px;
  transition: background-color 0.3s ease;
}
.confirm:hover {
  background-color: #c81e00;
}

/* Cancel Button */
.cancel {
  background-color: gray;
  color: white;
  padding: 8px 16px;
  border: none;
  cursor: pointer;
  border-radius: 5px;
  transition: background-color 0.3s ease;
}
.cancel:hover {
  background-color: #636363;
}
</style>

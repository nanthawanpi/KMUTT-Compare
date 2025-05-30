<script setup>
// Modal visibility state
import { defineProps, defineEmits } from 'vue';


const props = defineProps({
  isVisible: {
    type: Boolean,
    required: true
  },
  formOd:{
    type: Number,
  },
  dormId: {
    type: Number,
  },
  context:{
    type: String,
    required: true
  }
});

const emit = defineEmits(['close', 'confirm','delete', 'cancel']);

const closeModal = () => {
  emit('close');
};

// Function to handle the confirmation action
const confirmAction = () => {
  emit('confirm', props.context);  // Emit "confirm" event with context to parent
  closeModal();
};


const confirmDelete = () => {
  emit('delete');  // ส่ง event 'delete' เมื่อกดยืนยัน
  closeModal();
};

const confirmCancel = () => {
  emit('cancel');  // ส่ง event 'cancel' เมื่อกดยืนยัน
  closeModal();
  
};
</script>

<template>
  <div v-if="isVisible" class="popup-overlay">
    <div class="filter">
      <div class="border rounded-lg shadow relative max-w-md bg-white">
        <div class="flex justify-end p-2">
          <button type="button" class="text-gray-400 bg-transparent hover:bg-gray-200 hover:text-gray-900 rounded-lg text-sm p-1.5 ml-auto inline-flex items-center" @click="closeModal">
            <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
              <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"></path>
            </svg>
          </button>
        </div>
        <div class="p-6 pt-0 text-center">
          <svg v-if="context === 'delete' || context === 'cancel' || context === 'deleteUser' " class="w-20 h-20 text-red-600 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
          </svg>
          <svg v-if="context === 'add'" class="w-20 h-20 text-green-600 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
          </svg>
          <svg v-if="context === 'update'" class="w-20 h-20 text-blue-600 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 2H8a2 2 0 00-2 2v16a2 2 0 002 2h8a2 2 0 002-2V4a2 2 0 00-2-2z"></path>
          </svg>
          <h3 class="text-xl font-normal text-gray-500 mt-5 mb-6">
            <span v-if="context === 'delete'">คุณต้องการลบหอพักนี้จริงๆ หรือไม่? กระบวนการนี้ไม่สามารถย้อนกลับได้</span>
            <span v-if="context === 'add'">คุณแน่ใจหรือไม่ว่าต้องการเพิ่มหอพักนี้ ?</span>
            <span v-if="context === 'update'">คุณแน่ใจหรือไม่ว่าต้องการอัปเดตข้อมูลหอพักนี้ ?</span>
            <span v-if="context === 'cancel'">คุณต้องการยกเลิกการจองหอพักนี้จริงๆ ใช่ไหม? กระบวนการนี้ไม่สามารถย้อนกลับได้ ?</span>
            <span v-if="context === 'deleteUser'">คุณต้องการลบบัญชีผู้ใช้นี้จริงๆ หรือไม่? กระบวนการนี้ไม่สามารถย้อนกลับได้</span>
          </h3>
            <div class="flex flex-row items-center justify-center">
              <div class="w-1/2">
                <a href="#" @click="confirmDelete" class="text-white bg-red-600 hover:bg-red-800 focus:ring-4 focus:ring-red-300 font-medium rounded-lg text-base inline-flex items-center px-10 py-3 text-center mr-2" v-if="context === 'delete' || context === 'deleteUser'">
                  ใช่, ฉันแน่ใจ
                </a>
                <a href="#" @click="confirmCancel" class="text-white bg-red-600 hover:bg-red-800 focus:ring-4 focus:ring-red-300 font-medium rounded-lg text-base inline-flex items-center px-10 py-3 text-center mr-2" v-if="context === 'cancel'">
                  ใช่, ฉันแน่ใจ
                </a>
                <a href="#" @click="confirmAction" class="text-white bg-green-600 hover:bg-green-800 focus:ring-4 focus:ring-green-300 font-medium rounded-lg text-base inline-flex items-center px-12 py-3 text-center mr-2" v-if="context === 'add'">
                  ยืนยัน
                </a>
                <a href="#" @click="confirmAction" class="text-white bg-blue-600 hover:bg-blue-800 focus:ring-4 focus:ring-blue-300 font-medium rounded-lg text-base inline-flex items-center px-12 py-3 text-center mr-2" v-if="context === 'update'">
                  ยืนยัน
                </a>
              </div>
              <div class="w-1/2">
                <a href="#" @click="closeModal" class="text-gray-900 bg-gray-200 hover:bg-gray-300 focus:ring-4 focus:ring-gray-300 font-medium rounded-lg text-base inline-flex items-center px-12 py-3 text-center mr-2">
                  ยกเลิก
                </a>
              </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.popup-overlay {
  position: fixed; /* ให้ modal อยู่บนสุด */
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5); /* เพิ่มสีพื้นหลังโปร่งแสง */
  display: flex;
  justify-content: center; /* จัดให้ modal อยู่กลางหน้าจอ */
  align-items: center;
  z-index: 9999; /* ให้อยู่เหนือคอนเทนต์อื่นๆ */
}

.filter {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px; /* เพิ่มระยะห่างจากขอบ */
}

.border {
  width: 100%; /* ขยาย modal ให้กว้างขึ้น */
  max-width: 500px; /* กำหนดขนาดสูงสุด */
  padding: 20px;
  border-radius: 8px;
  background-color: white;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}
</style>

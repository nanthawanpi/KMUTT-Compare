<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import router from '@/router';

import { useUIStore } from '@/stores/uiStore';
import LoginPopup from '@/components/popups/LoginPopup.vue';
import RegisterPopup from './popups/RegisterPopup.vue';

import { clearToken } from '@/composables/Authentication/clearToken';
import { useAuthorize } from '@/stores/authorize';


const isLogoutConfirmVisible = ref(false);

const openLogoutConfirm = () => {
  isLogoutConfirmVisible.value = true;
};

const closeLogoutConfirm = () => {
  isLogoutConfirmVisible.value = false;
};

const confirmLogout = () => {
  clearToken();
  // Add any additional logic for logging out the user, such as redirecting
  closeLogoutConfirm();
  window.location.reload();
  
};

const authStore = useAuthorize()


const isMenuOpen = ref(false);
const isDropdownOpen = ref(false);

const toggleDropdown = () => {
  isDropdownOpen.value = !isDropdownOpen.value;
};

const closeDropdown = (event) => {
  if (!event.target.closest('#dropdownInformationButton') && !event.target.closest('#dropdownInformation')) {
    isDropdownOpen.value = false;
  }
};

onMounted(() => {
  window.addEventListener('click', closeDropdown);
  // console.log("userRole Right Now:"+authStore.userRole)
});

onBeforeUnmount(() => {
  window.removeEventListener('click', closeDropdown);
});


const uiStore = useUIStore();

const openLoginPopup = () => {
  uiStore.openLoginPopup();
};

const openRegisPopup = () => {
  uiStore.openRegisPopup();
};

const clickProfile = () => {
  toggleDropdown()
  router.push('/profile')
}

const clickDormlists = ()=>{
  toggleDropdown()
  router.push('/dormlists')
}

const clickReservedForms = ()=>{
  toggleDropdown()
  router.push('/reservedForms')
}

const clickReceivedForms = ()=>{
  toggleDropdown()
  router.push('/receivedForms')
}

const clickSupport = ()=>{
  toggleDropdown()
  router.push('/support')
}









</script>

<template>
  <nav class="relative bg-white dark:bg-gray-900 fixed w-full z-20 top-0 start-0 border-b border-gray-200 dark:border-gray-600">
    <div class="max-w-screen-xl flex flex-wrap items-center justify-between mx-auto p-4">
      
      <a href="#"><img src="./icons/logoKmutt.png" class="w-20" alt="icon"></a>
      
      <div class="flex md:order-2 space-x-3 md:space-x-4 rtl:space-x-reverse">
        <div v-if="authStore.userRole=='guest'"><button type="button" @click="openLoginPopup" class="text-gray-900 dark:text-white bg-gray-200 hover:bg-gray-300 dark:bg-gray-700 dark:hover:bg-gray-600 font-medium rounded-lg text-sm px-4 py-2 text-center">เข้าสู่ระบบ</button></div>
        <div v-if="authStore.userRole !== 'guest'"><button type="button" @click="router.push('/addEditDormitory')" class="text-white bg-orange-500 hover:bg-orange-600 font-medium rounded-lg text-sm px-4 py-2 text-center dark:bg-orange-600 dark:hover:bg-orange-700 dark:focus:ring-orange-800">เพิ่มหอพัก</button></div>
        
        <button v-if="authStore.userRole=='guest'"  type="button" @click="openRegisPopup" class="text-white bg-orange-500 hover:bg-orange-600 font-medium rounded-lg text-sm px-4 py-2 text-center dark:bg-orange-600 dark:hover:bg-orange-700 dark:focus:ring-orange-800">สมัครสมาชิก</button>
        
        <button @click="isMenuOpen = !isMenuOpen" data-collapse-toggle="navbar-sticky" type="button" class="inline-flex items-center p-2 w-10 h-10 justify-center text-sm text-gray-500 rounded-lg md:hidden hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-gray-200 dark:text-gray-400 dark:hover:bg-gray-700 dark:focus:ring-gray-600" aria-controls="navbar-sticky" aria-expanded="false">
          <span class="sr-only">Open main menu</span>
          <svg class="w-5 h-5" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 17 14">
            <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M1 1h15M1 7h15M1 13h15"/>
          </svg>
        </button>
  
        <div v-if="authStore.userRole !== 'guest'" class="flex flex-row">
          <p class="py-2.5"><img src="../components/icons/line.png" alt=""></p>
            <button @click="toggleDropdown" id="dropdownInformationButton" class="font-medium rounded-lg text-sm px-5 py-2.5 text-center inline-flex items-center hover:text-gray-600">
              <p>{{authStore.username}}</p> 
              <span class="text-orange-500 pl-1">({{ authStore.userRole }}) </span>  <!-- แสดง Role ที่นี่ -->
              <svg class="w-2.5 h-2.5 ms-3" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 10 6">
                <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="m1 1 4 4 4-4"/>
              </svg>
            </button>
  
            <!-- Dropdown menu -->
            <div v-if="isDropdownOpen" id="dropdownInformation" class="w-56 absolute z-10 bg-white divide-y divide-gray-100 rounded-lg shadow w-44">
              <div @click="toggleDropdown" class="px-4 py-3 text-sm text-gray-900 cursor-pointer">
                <div class="flex items-center">
                  <div class="flex w-full">{{authStore.username}}<span class="text-orange-500 pl-1">({{ authStore.userRole }})</span></div>
                  <div class="flex w-full justify-end">
                    <svg class="w-2.5 h-2.5 ms-3 ml-auto" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 10 6">
                      <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="m1 1 4 4 4-4"/>
                    </svg>
                  </div>
                </div>
              </div>
              <ul class="py-2 text-sm text-gray-700">
                <li>
                  <p @click="clickProfile"  class="block px-4 py-2 hover:bg-gray-100 cursor-pointer">โปรไฟล์</p>
                </li>
                <li>
                  <p @click="clickDormlists" class="block px-4 py-2 hover:bg-gray-100 cursor-pointer">รายการประกาศ</p>
                </li>
                <li>
                  <p @click="clickReservedForms" class="block px-4 py-2 hover:bg-gray-100 cursor-pointer">หอพักที่จอง</p>
                </li>
                <li>
                  <p @click="clickReceivedForms" class="block px-4 py-2 hover:bg-gray-100 cursor-pointer">ฟอร์มที่ได้รับ</p>
                </li>
                <li>
                  <p @click="clickSupport" class="block px-4 py-2 hover:bg-gray-100 cursor-pointer">ความช่วยเหลือและการสนับสนุน</p>
                </li>
              </ul>
              <div class="py-2">
                <p @click="openLogoutConfirm" class="cursor-pointer block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">ออกจากระบบ</p>
              </div>
            </div>
  
        </div>
  
      </div>
<!-- Items bar -->
<div
  id="navbar-sticky"
  :class="[
    'transition-all duration-300 ease-in-out md:static md:flex md:w-auto md:order-1',
    isMenuOpen ? 'absolute top-full left-0 w-full bg-white shadow-lg z-30' : 'hidden'
  ]"
>
  <ul class="flex flex-col md:flex-row md:space-x-10 font-medium border border-gray-100 md:border-0 rounded-lg md:rounded-none bg-gray-50 md:bg-white p-4 md:p-0">
    <li v-if="authStore.userRole === 'admin'">
      <p @click="isMenuOpen = false; $router.push('/admin/dashboard')" class="cursor-pointer hover:bg-gray-100 active:bg-gray-200">แดชบอร์ด</p>
    </li>
    <li>
      <p @click="isMenuOpen = false; $router.push('/')" class="cursor-pointer hover:bg-gray-100 active:bg-gray-200">หน้าหลัก</p>
    </li>
    <li v-if="authStore.userRole === 'admin' || authStore.userRole === 'user'">
      <p @click="isMenuOpen = false; $router.push('/favorites')" class="cursor-pointer hover:bg-gray-100 active:bg-gray-200">รายการโปรด</p>
    </li>
    <li>
      <p @click="isMenuOpen = false; $router.push('/faq')" class="cursor-pointer hover:bg-gray-100 active:bg-gray-200">ช่วยเหลือ</p>
    </li>
    <li>
      <p @click="isMenuOpen = false; $router.push('/contact')" class="cursor-pointer hover:bg-gray-100 active:bg-gray-200">ติดต่อเรา</p>
    </li>
  </ul>
</div>



    </div>
    <LoginPopup v-if="uiStore.isLoginPopupOpen"/>
    <RegisterPopup v-if="uiStore.isRegisPopupOpen"/>
  </nav>

   <!-- Logout Confirmation Popup -->
   <div v-if="isLogoutConfirmVisible" class="popup-overlay">
    <div class="filter">
      <div class="border-logout rounded-lg shadow relative max-w-md bg-white">
        <div class="flex justify-end p-2">
          <button type="button" class="text-gray-400 bg-transparent hover:bg-gray-200 hover:text-gray-900 rounded-lg text-sm p-1.5 ml-auto inline-flex items-center" @click="closeLogoutConfirm">
            <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
              <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"></path>
            </svg>
          </button>
        </div>
        <div class="p-6 pt-0 text-center">
          <svg class="w-20 h-20 text-red-600 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
          </svg>
          <h3 class="text-xl font-normal text-gray-500 mt-5 mb-6">
            คุณแน่ใจหรือไม่ว่าต้องการออกจากระบบ?
          </h3>
          <div class="flex flex-row items-center justify-center">
            <div class="w-1/2">
              <a href="#" @click="confirmLogout" class="text-white bg-red-600 hover:bg-red-800 focus:ring-4 focus:ring-red-300 font-medium rounded-lg text-base inline-flex items-center px-10 py-3 text-center mr-2">
                ใช่, ฉันแน่ใจ
              </a>
            </div>
            <div class="w-1/2">
              <a href="#" @click="closeLogoutConfirm" class="text-gray-900 bg-gray-200 hover:bg-gray-300 focus:ring-4 focus:ring-gray-300 font-medium rounded-lg text-base inline-flex items-center px-12 py-3 text-center mr-2">
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

/* Add any custom styles here */
@media(min-width: 50px){
  .icon{
    height: 40px;
  }
}

@media(min-width: 640px){
  .icon{
    height: 50px;
  }
}

@media(min-width: 1280px){
  .icon{
    height: 50px;
  }
}
.popup-overlay {
  position: fixed; /* ให้ modal อยู่บนสุด */
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.7); /* เพิ่มสีพื้นหลังโปร่งแสง */
  display: flex;
  justify-content: center; /* จัดให้ modal อยู่กลางหน้าจอ */
  align-items: center;
  z-index: 9999; /* ให้อยู่เหนือคอนเทนต์อื่นๆ */
}

.filter {
  width: 100%;
  height: 100%; /* กำหนดความสูงเต็มจอ */
  display: flex;
  justify-content: center;
  align-items: center;
}

.popup-overlay {
  position: fixed; /* ให้ modal อยู่บนสุด */
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.7); /* เพิ่มสีพื้นหลังโปร่งแสง */
  display: flex;
  justify-content: center; /* จัดให้ modal อยู่กลางหน้าจอ */
  align-items: center;
  z-index: 9999; /* ให้อยู่เหนือคอนเทนต์อื่นๆ */
}

.filter {
  width: 100%;
  height: 100%; /* กำหนดความสูงเต็มจอ */
  display: flex;
  justify-content: center;
  align-items: center;
}

.border-logout{
  width: 100%;
  max-width: 500px; /* กำหนดขนาดสูงสุด */
  padding: 20px;
  border-radius: 8px;
  background-color: white;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}
</style>

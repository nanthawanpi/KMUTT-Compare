<script setup>
</script>

<template>
  <div aria-label="Sidebar">
    <div class="px-3 overflow-y-auto bg-gray-50 dark:bg-gray-800">
      <ul class="space-y-2 font-medium">
        <li>
          <router-link to="profile" 
                       class="flex items-center p-2 text-gray-900 rounded-lg dark:text-white hover:bg-gray-100 dark:hover:bg-gray-700 group"
                       :class="{ 'bg-gray-700': $route.path === '/profile' }">
            <img class="w-7" src="./icons/Profile/user_icon.png" alt="">
            <span class="ms-3">แก้ไขโปรไฟล์</span>
          </router-link>
        </li>
        <li>
          <router-link to="dormlists" 
                       class="flex items-center p-2 text-gray-900 rounded-lg dark:text-white hover:bg-gray-100 dark:hover:bg-gray-700 group"
                       :class="{ 'bg-gray-700': $route.path === '/dormlists' }">
            <img class="w-7" src="./icons/Profile/list_icon.png" alt="">
            <span class="flex-1 ms-3 whitespace-nowrap">รายการประกาศ</span>
          </router-link>
        </li>
        <li>
          <router-link to="reservedForms" 
                       class="flex items-center p-2 text-gray-900 rounded-lg dark:text-white hover:bg-gray-100 dark:hover:bg-gray-700 group"
                       :class="{ 'bg-gray-700': $route.path === '/reservedForms' }">
            <img class="w-7" src="./icons/Profile/reservedforms_icon.png" alt="">
            <span class="flex-1 ms-3 whitespace-nowrap">หอพักที่จอง</span>
          </router-link>
        </li>
        <li>
          <router-link to="receivedForms" 
                       class="flex items-center p-2 text-gray-900 rounded-lg dark:text-white hover:bg-gray-100 dark:hover:bg-gray-700 group"
                       :class="{ 'bg-gray-700': $route.path === '/receivedForms' }">
            <img class="w-7" src="./icons/Profile/receivedforms_icon.png" alt="">
            <span class="flex-1 ms-3 whitespace-nowrap">ฟอร์มที่ได้รับ</span>
          </router-link>
        </li>
        <li>
          <router-link to="support" 
                       class="flex items-center p-2 text-gray-900 rounded-lg dark:text-white hover:bg-gray-100 dark:hover:bg-gray-700 group"
                       :class="{ 'bg-gray-700': $route.path === '/support' }">
            <img class="w-7" src="./icons/Profile/support_icon.png" alt="">
            <span class="flex-1 ms-3 whitespace-nowrap">support</span>
          </router-link>
        </li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
/* Optional styles for active link */
.router-link-active {
  background-color: #eeeeee; /* Gray-700 */
}
</style>

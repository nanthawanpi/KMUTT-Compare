<script setup>
import { ref, onMounted, computed } from 'vue';
import Sidebar from '@/components/Sidebar.vue';
import { formatDate } from '@/composables/formatDate';
import { formatPhoneNumber } from '@/composables/formatPhoneNumber';
import { useReceivedForms } from '@/composables/getReceivedForms';
import SuccessModal from '@/components/modals/SuccessModal.vue';

const API_ROOT = import.meta.env.VITE_API_ROOT;
const { fetchReceivedForms, receivedForms, isLoading } = useReceivedForms();


// Sort submitted forms by form_date
const sortedForms = computed(() => {
  return receivedForms.value.sort((a, b) => {
    const dateA = new Date(a.form_date);
    const dateB = new Date(b.form_date);
    return dateB - dateA;  // ถ้าคุณต้องการเรียงจากใหม่ไปเก่า
  });
});

// Pagination
const currentPage = ref(1);
const itemsPerPage = ref(3); // จำนวนรายการต่อหน้า

// คำนวณหน้าทั้งหมด
const totalPages = computed(() => Math.ceil(sortedForms.value.length / itemsPerPage.value));

// คำนวณรายการที่จะแสดงในหน้านี้
const paginatedForms = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  const end = start + itemsPerPage.value;
  return sortedForms.value.slice(start, end);
});

// เปลี่ยนหน้า
const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
  }
};


onMounted(() => {
  fetchReceivedForms();
});

// -------------------------------- Success Modal --------------------------------
const isModalOpen = ref(false)
const modalData = ref({ title: '', message: '', context: '' });

// -------------------------------- Show Detail --------------------------------
const isShowDetail = ref(false); 

const toggleDetail = () => {
  isShowDetail.value = !isShowDetail.value;
};


// -------------------------------- Show Detail --------------------------------
const isLoadingCheckIn = ref(false); 

const checkIn = async (formId) => {
  isLoadingCheckIn.value = true; // เมื่อเริ่มการเช็คอินให้แสดงสถานะโหลด

  try {
    const response = await fetch(`${API_ROOT}/user/form/${formId}/check-in`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({ status: 'checkIn' })
    });

    // ตรวจสอบสถานะ response และแสดงข้อความตามประเภทของ response
    if (response.ok) {
      modalData.value = {
          title: `เช็คอินสำเร็จ`,
          message: `อัปเดตสถานะผู้เข้าพักเรียบร้อยแล้ว`,
          context: 'checkIn'
        };
        isModalOpen.value = true
      fetchReceivedForms()
    } else {
      alert(`ไม่สามารถเช็คอินได้`);
    }
  } catch (error) {
    // console.error("Error Check-In:", error);
    // alert("เกิดข้อผิดพลาดในการเช็คอิน");
  } finally {
    isLoadingCheckIn.value = false; // เมื่อเสร็จสิ้นการเช็คอินให้ซ่อนสถานะโหลด
  }
};
</script>
<template>
  <div class="flex flex-row w-full justify-center p-20">
    <Sidebar class="hidden lg:block w-64" />
    <div class="pl-0 lg:pl-2 w-full lg:w-1/2 h-full">
      <div class="p-6 border-2 border-gray-200 border-dashed rounded-lg dark:border-gray-700">
        <h1 class="font-bold text-3xl mb-8">ฟอร์มที่ส่งแล้ว</h1>

        <div v-if="isLoading" class="text-center text-gray-600">
          กำลังโหลดข้อมูล...
        </div>

        <div v-else-if="paginatedForms.length === 0" class="text-center text-gray-600">
          ยังไม่มีฟอร์มที่ได้รับ
        </div>

        <ul v-else class="space-y-6">
          <li v-for="form in paginatedForms" :key="form.id"
              :class="{'bg-gray-300': form.description.includes('ยกเลิกการจอง')}"
              class="p-6 rounded-lg shadow-md bg-white space-y-4">

            <div class="flex flex-row items-center justify-between">
              <h2 class="font-semibold text-lg">{{ form.dormName || 'ไม่มีชื่อหอพัก' }}</h2>
              <h2 class="text-sm text-gray-600">วันที่ส่ง: {{ formatDate(form.form_date) }}</h2>
            </div>

            <hr class="my-4">
              <!-- ข้อมูลผู้เข้าพัก -->
              <div class="rounded-lg">
                <div class="flex flex-row justify-between">
                  <div>
                    <p class="text-sm text-gray-600">ชื่อผู้จองหอพัก: {{ form.name || 'ไม่มีข้อมูล' }}</p>
                  </div>
                  <div class="text-lg text-blue-900 font-semibold cursor-pointer hover:text-black hover:scale-105 underline" @click="toggleDetail">
                    {{ isShowDetail ? 'ซ่อนรายละเอียด' : 'รายละเอียดเพิ่มเติม' }}
                  </div>
                </div>
                
                <p class="text-sm text-gray-600">เบอร์ติดต่อ: {{ formatPhoneNumber(form.phone || 'ไม่มีข้อมูล') }}</p>
                <p class="text-sm text-gray-600">อีเมล: {{form.email || 'ไม่มีข้อมูล' }}</p>
              </div>

              
              <!-- หาก isShowDetail เป็น true จะแสดงรายละเอียด -->
              <div v-if="isShowDetail" class="mt-4 p-4 bg-gray-100 rounded-lg">
                <p v-if="form.owner" class="text-sm text-gray-600">ชื่อเจ้าของหอพัก: {{ form.owner.name || 'ไม่มีข้อมูล' }}</p>
                <p v-if="form.owner" class="text-sm text-gray-600">เบอร์ติดต่อ: {{ form.owner.phone || 'ไม่มีข้อมูล' }}</p>
                <p v-if="form.owner" class="text-sm text-gray-600">อีเมล: {{ form.owner.email || 'ไม่มีข้อมูล' }}</p>
                <p class="text-sm text-gray-600">รายละเอียดเพิ่มเติม: {{ form.description || 'ไม่มีข้อมูล' }}</p>
                <p class="text-sm text-gray-600">วันที่เข้าพัก: {{ formatDate(form.date_in || 'ไม่มีข้อมูล') }}</p>
                <p class="text-sm text-gray-600">วันที่ออก: {{ formatDate(form.date_out|| 'ไม่มีข้อมูล') }}</p>
              </div>

            <!-- ปุ่มแก้ไขและยกเลิกจะถูกซ่อนไปหาก description มีคำว่า 'ยกเลิกการจอง' -->
            <div v-if="!form.description.includes('ยกเลิกการจอง')" class="flex justify-end space-x-6 mt-6">
              
              <!-- ปุ่มเช็คอินแสดงข้อความโหลดหรือปกติ -->
                <button v-if="form.status === 'reserved'" class="btn bg-green-500 text-white p-3 hover:bg-green-600 hover:text-white w-32" @click="checkIn(form.formId)" :disabled="isLoadingCheckIn">
                  <span v-if="isLoadingCheckIn">กำลังเช็คอิน...</span>
                  <span v-else>เช็คอิน</span>
                </button>

               <!-- ให้คะแนนแล้ว -->
               <p  v-if="form.status === 'checkIn'">⏳ รอคะแนนรีวิว </p>

               <!-- ให้คะแนนแล้ว -->
              <p  v-if="form.status === 'success'">✔️ ผู้เข้าพักให้คะแนนแล้ว</p>
            </div>
            <!-- ข้อความแสดงเมื่อ description มีคำว่า 'ยกเลิกการจอง' -->
            <div v-if="form.description.includes('ยกเลิกการจอง')" class="text-right text-gray-500 mt-4">
              ยกเลิกการจองแล้ว
            </div>
          </li>
        </ul>

        <!-- Pagination -->
        <div class="flex justify-center items-center space-x-4 mt-6">
          <button 
            @click="goToPage(currentPage - 1)" 
            :disabled="currentPage === 1" 
            class="btn-pagination bg-orange-500 text-white hover:text-white hover:bg-orange-600 disabled:text-gray-400 disabled:bg-gray-200 py-2 px-4 rounded-md shadow-md transition duration-200">
            «
          </button>
          
          <span class="text-gray-700 font-semibold text-lg">หน้า {{ currentPage }} จาก {{ totalPages }}</span>
          
          <button 
            @click="goToPage(currentPage + 1)" 
            :disabled="currentPage === totalPages" 
            class="btn-pagination bg-orange-500 text-white hover:text-white hover:bg-orange-600 disabled:text-gray-400 disabled:bg-gray-200 py-2 px-4 rounded-md shadow-md transition duration-200">
            »
          </button>
        </div>


      </div>
    </div>
  </div>

  <SuccessModal 
    v-if="isModalOpen" 
    :title="modalData.title" 
    :message="modalData.message" 
    :context="modalData.context" 
    @close="isModalOpen = false"
/>


</template>




<style scoped>
h1{
    font-size: 2rem;
    color: #2b2b2b; 
}


p {
    font-size: 1.1rem;
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

/* เพิ่มคลาสให้ฟอร์มสีเทาเมื่อ description มีคำว่า 'ยกเลิกการจอง' */
.bg-gray-300 {
  background-color: #f3f3f3;
}

</style>

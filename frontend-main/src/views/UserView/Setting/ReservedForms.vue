<script setup>
import { ref, onMounted, computed } from 'vue';
import Sidebar from '@/components/Sidebar.vue';
import { formatDate } from '@/composables/formatDate';
import { formatPhoneNumber } from '@/composables/formatPhoneNumber';
import { useRouter } from 'vue-router';  // Import vue-router for navigation
import { useReservedForms } from '@/composables/getReservedForms';
import ConfirmModal from '@/components/modals/ConfirmModal.vue';
import SuccessModal from '@/components/modals/SuccessModal.vue';

const API_ROOT = import.meta.env.VITE_API_ROOT;

const isModalVisible = ref(false);
const formIdToCancel = ref(null);

// Show the modal when cancel button is clicked
const showConfirmModal = (formId) => {
  formIdToCancel.value = formId;  // Store the formId to cancel
  isModalVisible.value = true;  // Show the modal
};

const closeModal = () => {
  isModalVisible.value = false;
  formIdToCancel.value = null;  // Reset formId when modal is closed
};


// Sort submitted forms by form_date
const sortedForms = computed(() => {
  return reservedForms.value.sort((a, b) => {
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



const { fetchReservedForms, reservedForms,isLoading } = useReservedForms();

// Create a router instance
const router = useRouter();


// Function to navigate to the edit page
const goToEditPage = (formId) => {
  router.push({
    name : 'reservation',
    params : {id : formId, action: 'edit'}
  })
};


// Modify handleCancelBooking to accept formId and action
const handleCancelBooking = () => {
  router.push({ name: "reservation", params: { id: formIdToCancel.value, action: 'cancel' } });
};


onMounted(() => {
  fetchReservedForms();
});

// -------------------------------- Success Modal --------------------------------
const isModalOpen = ref(false)
const modalData = ref({ title: '', message: '', context: '' });

const isShowDetail = ref(false); // เริ่มต้นคือไม่แสดงรายละเอียด

// ฟังก์ชั่นเพื่อสลับสถานะของ isShowDetail
const toggleDetail = () => {
  isShowDetail.value = !isShowDetail.value;
};


const isRatingModalOpen = ref(false)
const selectedScore = ref(0);
const hoveredScore = ref(0);

const setScore = (score) => {
  selectedScore.value = score;
};

const hoverStar = (score) => {
  hoveredScore.value = score;
};

const resetHover = () => {
  hoveredScore.value = 0;
};

const closeRatingModal = () => {
  isRatingModalOpen.value = false;
};

const dormId = ref(0)

const voteRating = (id) =>{
  dormId.value = id
  // console.log(dormId.value)
  isRatingModalOpen.value =! isRatingModalOpen.value

}

const submitRating = async () => {
  // console.log("Submitted rating:", selectedScore.value);

  try {
    const response = await fetch(`${API_ROOT}/user/votes/dormitory/${dormId.value}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({ totalScore: selectedScore.value })
    });

    // ตรวจสอบสถานะ response และแสดงข้อความตามประเภทของ response
    if (response.ok) {
      modalData.value = {
          title: `ให้คะแนนสำเร็จ`,
          message: `ขอบคุณสำหรับการให้คะแนน! เราหวังว่าคุณจะได้รับประสบการณ์ที่ดี`,
          context: 'reservation'
        };
        isModalOpen.value = true
      fetchReservedForms()
      closeRatingModal();
      
    } else {
      alert(`ไม่สามารถโหวตได้`);
      closeRatingModal();
    }
  } catch (error) {
    // console.error("Error submitting rating:", error);
    // alert("เกิดข้อผิดพลาดในการโหวต");
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
          ยังไม่มีฟอร์มที่ส่ง
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
                    <p v-if="form.owner" class="text-sm text-gray-600">ชื่อเจ้าของหอพัก: {{ form.owner.name || 'ไม่มีข้อมูล' }}</p>
                  </div>
                  <div class="text-lg text-blue-900 font-semibold cursor-pointer hover:text-black hover:scale-105 underline" @click="toggleDetail">
                    {{ isShowDetail ? 'ซ่อนรายละเอียด' : 'รายละเอียดเพิ่มเติม' }}
                  </div>
                </div>
                
                <p v-if="form.owner" class="text-sm text-gray-600">เบอร์ติดต่อ: {{ formatPhoneNumber(form.owner.phone || 'ไม่มีข้อมูล') }}</p>
                <p v-if="form.owner" class="text-sm text-gray-600">อีเมล: {{form.owner.email || 'ไม่มีข้อมูล' }}</p>
              </div>

              
              <!-- หาก isShowDetail เป็น true จะแสดงรายละเอียด -->
              <div v-if="isShowDetail" class="mt-4 p-4 bg-gray-100 rounded-lg">
                <p class="text-sm text-gray-600">ชื่อผู้จองหอพัก: {{ form.name || 'ไม่มีข้อมูล' }}</p>
                <p class="text-sm text-gray-600">เบอร์ติดต่อ: {{ form.phone || 'ไม่มีข้อมูล' }}</p>
                <p class="text-sm text-gray-600">อีเมล: {{ form.email || 'ไม่มีข้อมูล' }}</p>
                <p class="text-sm text-gray-600">รายละเอียดเพิ่มเติม: {{ form.description || 'ไม่มีข้อมูล' }}</p>
                <p class="text-sm text-gray-600">วันที่เข้าพัก: {{ formatDate(form.date_in || 'ไม่มีข้อมูล') }}</p>
                <p class="text-sm text-gray-600">วันที่ออก: {{ formatDate(form.date_out|| 'ไม่มีข้อมูล') }}</p>
              </div>

            <!-- ปุ่มแก้ไขและยกเลิกจะถูกซ่อนไปหาก description มีคำว่า 'ยกเลิกการจอง' -->
            <div v-if="!form.description.includes('ยกเลิกการจอง')" class="flex justify-end space-x-6 mt-6">
              
              <!-- จองไว้ แต่ยังไม่เช็คอิน -->
              <button v-if="form.status === 'reserved' " class="btn p-3 bg-blue-500 text-white hover:bg-blue-600 w-32" @click="goToEditPage(form.formId)">
                แก้ไข
              </button>
              <button v-if="form.status === 'reserved' " class="btn p-3 bg-red-500 text-white hover:bg-red-600 w-32" type="button" 
                @click="showConfirmModal(form.formId)">
                ยกเลิกการจอง
              </button>

            <!-- เช็คอินแล้ว -->
              <button v-if="form.status === 'checkIn'" class="btn bg-white p-3 border border-orange-500 text-orange-500 hover:bg-orange-500 hover:text-white w-32" @click="voteRating(form.dormId)">
                ให้คะแนน
              </button>

               <!-- ให้คะแนนแล้ว -->
              <p  v-if="form.status === 'success'">✔️ ให้คะแนนแล้ว</p>
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



              <!-- Modal ให้คะแนน -->
              <div v-if="isRatingModalOpen" class="fixed inset-0 flex items-center justify-center bg-gray-900 bg-opacity-50">
                <div class="bg-white p-6 rounded-lg shadow-lg w-96">
                  <h2 class="text-2xl text-center">คุณพอใจกับการใช้บริการหอพักนี้มากแค่ไหน</h2>

                  <div class="flex justify-center space-x-2 mt-4">
                    <span
                      v-for="star in 5"
                      :key="star"
                      class="cursor-pointer transition-transform duration-200 transform"
                      :class="{
                        'scale-125': star === hoveredScore  // ขยายขนาดเมื่อ hover
                      }"
                      @click="setScore(star)"
                      @mouseover="hoverStar(star)"
                      @mouseleave="resetHover"
                    >
                      <img 
                        :src="star <= (hoveredScore || selectedScore) ? '/star.png' : '/star3.png'" 
                        alt="ดาว" 
                        class="w-10"
                      >
                    </span>
                  </div>



                  <div class="flex justify-between mt-6">
                    <button @click="closeRatingModal" class="px-4 py-2 bg-gray-400 text-white rounded-lg hover:bg-gray-500">
                      ยกเลิก
                    </button>
                    <button @click="submitRating(dormId)" class="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600" :disabled="!selectedScore">
                      ส่งคะแนน
                    </button>
                  </div>
                </div>
              </div>

  </div>

  <ConfirmModal
    :isVisible="isModalVisible" 
    :formId="formIdToCancel" 
    @close="closeModal" 
    @cancel="handleCancelBooking" 
    context="cancel" 
  />

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
   color: #333;
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

import { ref } from 'vue';
const API_ROOT = import.meta.env.VITE_API_ROOT;
import { getNewToken } from "@/composables/Authentication/getNewToken";

export const useReceivedForms = () => {
  const isLoading = ref(true);
  const receivedForms = ref([]);

  const fetchReceivedForms = async () => {
    try {
      const response = await fetch(`${API_ROOT}/user/receive-form`, {
        method: 'GET',
        headers: {
          "Content-Type": "application/json",
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });

      // ถ้าสถานะเป็น 401, รีเฟรช token และลองใหม่
      if (response.status === 401 || response.status === 403) {
        await getNewToken(); // รีเฟรช token ใหม่
        response = await fetch(`${API_ROOT}/forms/user`, {  // 🔥 ใช้ endpoint เดิม
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + localStorage.getItem("token"),
          },
        });
      }

      // ตรวจสอบว่าการตอบกลับจาก API ถูกต้องหรือไม่
      if (!response.ok) throw new Error('ไม่สามารถโหลดข้อมูลได้');

      const data = await response.json();

      // ถ้าไม่มีข้อมูลให้ตั้งค่าเป็น [] (array ว่าง)
      if (data && Array.isArray(data) && data.length > 0) {
        receivedForms.value = data; // เก็บข้อมูลลงใน reactive variable
      } else {
        receivedForms.value = []; // กรณีไม่มีข้อมูล
      }
    } catch (error) {
      // console.error('เกิดข้อผิดพลาดในการโหลดข้อมูล:', error);
      receivedForms.value = []; // กรณีเกิดข้อผิดพลาดในการดึงข้อมูล
    } finally {
      isLoading.value = false;
    }
  };

  return {
    isLoading,
    receivedForms,
    fetchReceivedForms
  };
};

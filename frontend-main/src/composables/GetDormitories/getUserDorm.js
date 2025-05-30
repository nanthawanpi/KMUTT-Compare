import { getNewToken } from "@/composables/Authentication/getNewToken";
const API_ROOT = import.meta.env.VITE_API_ROOT;

// ฟังก์ชันสำหรับดึงข้อมูล dormitories ของ user โดยใช้ userId ในระบบ
export const getUserDorm = async () => {
  try {
    let res = await fetch(`${API_ROOT}/dormitories/user`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + localStorage.getItem("token"),
      },
    });

    if (res.ok) {
      return await res.json();
    }

    if (res.status === 401 || res.status === 403) {
      await getNewToken(); // รีเฟรช token
      res = await fetch(`${API_ROOT}/dormitories/user`, {
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + localStorage.getItem("token"),
        },
      });

      if (res.ok) {
        return await res.json();
      }
    }

    return []; // คืนค่า [] แทน null
  } catch (error) {
    // console.error("Error:", error);
    return []; // คืนค่า [] ในกรณีที่เกิดข้อผิดพลาด
  }
};


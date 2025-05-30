import { getNewToken } from "@/composables/Authentication/getNewToken";

const API_ROOT = import.meta.env.VITE_API_ROOT;

// ฟังก์ชันสำหรับดึงข้อมูลของ user โดยใช้ userId ในระบบ
export const getUserById = async (userId) => {
  if (!userId) return null; // ถ้าไม่มี userId ให้คืนค่า null

  try {
    let res = await fetch(`${API_ROOT}/admin/user/${userId}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        'Authorization': "Bearer " + localStorage.getItem('token')
      }
    });

    if (res.ok) {
      return await res.json();
    }

    if (res.status === 401 || res.status === 403) {
      await getNewToken(); // รีเฟรช token
      res = await fetch(`${API_ROOT}/admin/user/${userId}`, {
        headers: {
          "Content-Type": "application/json",
          'Authorization': "Bearer " + localStorage.getItem('token')
        }
      });

      if (res.ok) {
        return await res.json();
      }
    }

    // console.error(`Error fetching user (ID: ${userId}): ${res.status}`);
    return null; 
  } catch (error) {
    // console.error('Error:', error);
    return null;
  }
};

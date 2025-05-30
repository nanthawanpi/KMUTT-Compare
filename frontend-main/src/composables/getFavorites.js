const API_ROOT = import.meta.env.VITE_API_ROOT;
import { getNewToken } from "./Authentication/getNewToken";

// ฟังก์ชันสำหรับดึงข้อมูล Favorite ของ user ที่กำลัง Login
const getFavorites = async () => {
  try {
    let res = await fetch(`${API_ROOT}/favorites/user`, {
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
      await getNewToken(); // รีเฟรช token ใหม่
      res = await fetch(`${API_ROOT}/favorites/user`, {  // 🔥 ใช้ endpoint เดิม
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + localStorage.getItem("token"),
        },
      });

      if (res.ok) {
        return await res.json();
      }
    }

    return []; // คืนค่า [] ถ้าข้อมูลผิดพลาด
  } catch (error) {
    // console.error("Error:", error);
    return []; // คืนค่า [] ถ้า request ล้มเหลว
  }
};

export { getFavorites };

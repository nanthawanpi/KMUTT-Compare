const API_ROOT = import.meta.env.VITE_API_ROOT;
import { getNewToken } from "../Authentication/getNewToken";

// ฟังก์ชันสำหรับดึงข้อมูล user ในระบบ
const getUsers = async () => {
  try {
    let res = await fetch(`${API_ROOT}/admin/users`, {
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
      res = await fetch(`${API_ROOT}/admin/users`, {
        headers: {
          "Content-Type": "application/json",
          'Authorization': "Bearer " + localStorage.getItem('token')
        }
      });

      if (res.ok) {
        return await res.json();
      }

    }

    // console.error(`Error fetching users: ${res.status}`);
    return []; 
  } catch (error) {
    // console.error('Error:', error);
    return [];
  }
};

export { getUsers };

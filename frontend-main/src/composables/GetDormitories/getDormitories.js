import { getNewToken } from "../Authentication/getNewToken";

const API_ROOT = import.meta.env.VITE_API_ROOT

// ฟังก์ชันสำหรับดึงข้อมูลหอพักทั้งหมด
const getDormitories = async () => {
  try {
    let res = await fetch(`${API_ROOT}/dormitories`, {
      method: "GET",
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

    // console.error(`Error fetching dormitories: ${res.status}`);
    return []; 
  } catch (error) {
    // console.error('Error:', error);
    return [];
  }
};


export { getDormitories }



import { useUIStore } from "@/stores/uiStore";
const uiStore = useUIStore()
import { clearAllToken } from "../Authentication/clearToken";
import { getNewToken } from "../Authentication/getNewToken";
const API_ROOT = import.meta.env.VITE_API_ROOT;
const fetchUserProfile = async () => {
    try {
      const response = await fetch(`${API_ROOT}/user/me`, {
        method: 'GET',
        headers: {
          'Authorization': "Bearer " + localStorage.getItem('token')
        }
      });
  
      if (response.status === 401 || response.status === 403) {
        // ถ้า token หมดอายุ ให้เรียก getNewToken
        await getNewToken();
        // หลังจากนั้นลองทำการดึงข้อมูลผู้ใช้ใหม่อีกครั้ง
        return await fetchUserProfile();
      
      }else if(response.status === 404){
        clearAllToken()
        alert('Please Login again!')
        uiStore.openLoginPopup()
      }
  
      if (!response.ok) {
        throw new Error('ไม่สามารถดึงข้อมูลผู้ใช้ได้');
      }
  
      const data = await response.json();
      return data;
  
    } catch (error) {
      // alert(error.message);
    }
  };

  export {fetchUserProfile}
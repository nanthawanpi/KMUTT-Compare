const API_ROOT = import.meta.env.VITE_API_ROOT;

// ฟังก์ชันสำหรับดึงข้อมูลหอพักตาม id
const getDormitoryById = async (id) => {
  try {
    const res = await fetch(`${API_ROOT}/dormitories/${id}`);
    if (res.ok) {
      const data = await res.json();
      if (!data) {
        // ถ้าไม่มีข้อมูลให้แสดง confirm popup
        if (window.confirm('ไม่พบข้อมูลหอพัก ต้องการกลับไปที่หน้าหลักไหม?')) {
          // ไปที่หน้า home เมื่อผู้ใช้กดยืนยัน
          window.location.href = '/'; // เปลี่ยนเส้นทางไปที่หน้า home
        }
        return null; // คืนค่า null ถ้าไม่มีข้อมูล
      }
      return data;
    } else {
      throw new Error('Failed to fetch dormitory');
    }
  } catch (error) {
    if (window.confirm('ไม่พบข้อมูลหอพัก ต้องการกลับไปที่หน้าหลักไหม?')) {
      // ไปที่หน้า home เมื่อผู้ใช้กดยืนยัน
      window.location.href = '/'; // เปลี่ยนเส้นทางไปที่หน้า home
    }
    // console.error('Error fetching data:', error);
    throw error;
  }
};

export { getDormitoryById };

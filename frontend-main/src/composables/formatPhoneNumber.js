// เพิ่ม - ระหว่างหมายเลขโทรศัพท์
const formatPhoneNumber = (phoneNumber) => {
    if (!phoneNumber) return ''; // ตรวจสอบว่า phoneNumber ไม่ใช่ null หรือ undefined
    return phoneNumber.replace(/(\d{3})(\d{3})(\d{4})/, '$1-$2-$3');
  };

  export {formatPhoneNumber}
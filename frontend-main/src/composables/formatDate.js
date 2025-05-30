//formatDate("2025-03-27T14:30:00Z")	"27 Mar 2025, 14:30"
const formatDate = (date) => {
    return new Date(date).toLocaleString('en-GB', {
      day: "numeric",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
      hour12: true
    });
  }
  
  //convertDate("2025-03-27T14:30:00Z")	"2025-03-27"
  const convertDate = (dateTime) => {
    let date = new Date(dateTime)
    let dateObj = date.toLocaleDateString().split('/')
    return dateTime === null ? undefined : Number(dateObj[2]) 
    + '-' + ("0" + (Number(dateObj[0]))).slice(-2) + '-' 
    + ("0" + Number(dateObj[1])).slice(-2) 
  }
  
  
  //convertTime("2025-03-27T14:30:00Z")	"14:30"
  const convertTime = (dateTime) => {
    let date = new Date(dateTime)
    return dateTime === null ? undefined 
    : date.toLocaleTimeString("en-US",{ hour : "2-digit",minute : "2-digit",hour12 : false })
  }

  const formatLocalDateTime = (date) => {
    const localDate = new Date(date);
  
    // แปลงให้เป็นรูปแบบ 'YYYY-MM-DDTHH:mm' ที่รองรับใน input type datetime-local
    const localDateTime = localDate.toISOString().slice(0, 16); // Use toISOString for ISO format and slice to get 'YYYY-MM-DDTHH:mm'
  
    return localDateTime;
  };
  

  export { formatDate, convertDate, convertTime, formatLocalDateTime }
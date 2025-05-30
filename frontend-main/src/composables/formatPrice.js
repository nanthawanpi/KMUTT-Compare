// แปลง format ราคา
function formatPrice(price) {
    return new Intl.NumberFormat().format(price);
  }
  
  export { formatPrice };
  
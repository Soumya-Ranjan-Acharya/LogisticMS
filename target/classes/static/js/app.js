// TruckMate — Frontend JS

//  Auto-refresh on orders page
if (document.getElementById('auto-refresh')) {
    let countdown = 5;
    const label = document.getElementById('refresh-countdown');
    setInterval(() => {
        countdown--;
        if (label) label.textContent = countdown;
        if (countdown <= 0) {
            location.reload();
        }
    }, 1000);
}

//  OTP toggle 
function showOtpSection() {
    document.getElementById('otp-section').style.display = 'block';
    document.getElementById('password-section').style.display = 'none';
}

function showPasswordSection() {
    document.getElementById('otp-section').style.display = 'none';
    document.getElementById('password-section').style.display = 'block';
}

//  Confirm dialog for destructive actions 
function confirmAction(message) {
    return confirm(message);
}

//  Flash message auto-dismiss 
document.addEventListener('DOMContentLoaded', () => {
    const alerts = document.querySelectorAll('.alert.auto-dismiss');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 500);
        }, 4000);
    });
});

//  Form validation (create order) 
const createOrderForm = document.getElementById('create-order-form');
if (createOrderForm) {
    createOrderForm.addEventListener('submit', (e) => {
        const required = ['goodsType', 'vehicleType', 'pickupLocation', 'dropLocation'];
        let valid = true;
        required.forEach(name => {
            const el = createOrderForm.querySelector(`[name="${name}"]`);
            if (el && !el.value.trim()) {
                el.style.borderColor = '#dc2626';
                valid = false;
            } else if (el) {
                el.style.borderColor = '';
            }
        });
        if (!valid) {
            e.preventDefault();
            alert('Please fill in all required fields.');
        }
    });
}

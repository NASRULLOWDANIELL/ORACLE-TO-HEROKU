document.getElementById('check-availability').addEventListener('click', function() {
    fetchAvailableSlots();
});

function fetchAvailableSlots() {
    const checkinDate = document.getElementById('checkin-date').value;
    fetch(`/api/slots?date=${checkinDate}`)
        .then(response => response.json())
        .then(data => {
            displayAvailableSlots(data);
        })
        .catch(error => {
            console.error('Error fetching slots:', error);
        });
}

function displayAvailableSlots(slots) {
    const slotsList = document.getElementById('slots-list');
    slotsList.innerHTML = ''; // Clear previous slots

    slots.forEach(slot => {
        const listItem = document.createElement('li');
        listItem.textContent = slot.slotdate;
        listItem.dataset.slotId = slot.slotid;
        listItem.addEventListener('click', function() {
            selectSlot(slot.slotid);
        });
        slotsList.appendChild(listItem);
    });

    document.getElementById('available-slots').classList.remove('hidden');
}

function selectSlot(slotId) {
    document.getElementById('slot_id').value = slotId;
    console.log('Selected slot ID:', slotId);
}

function submitBooking(event) {
    event.preventDefault();

    const bookingData = {
        name: document.getElementById('name').value,
        contact: document.getElementById('contact').value,
        checkinDate: document.getElementById('checkin-date').value,
        checkoutDate: document.getElementById('checkout-date').value,
        price: document.getElementById('price').value,
        slot_id: document.getElementById('slot_id').value
    };

    fetch('/api/book', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(bookingData)
    })
    .then(response => response.json())
    .then(data => {
        alert(data.message);
    })
    .catch(error => {
        console.error('Error creating booking:', error);
    });
}

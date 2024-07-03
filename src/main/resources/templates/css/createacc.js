
        const show_pw_btn = document.querySelector('#show-passwd');
        const pw_input = document.querySelector('#custpassword');

        show_pw_btn.addEventListener('click', () => {
            if (pw_input.type === 'password') {
                pw_input.type = 'text';
                show_pw_btn.innerHTML = '<img src="image/eye_closed.svg" alt="Hide Password" />';
            } else {
                pw_input.type = 'password';
                show_pw_btn.innerHTML = '<img src="image/eye_open.svg" alt="Show Password" />';
            }
        });

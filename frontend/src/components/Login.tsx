import React, {useState} from 'react';
import {Input} from './Input';

/**
 * Interface representing the properties for a Login component.
 *
 * This interface provides a structure for handling the submission of login
 * credentials, including an optional callback function.
 *
 * @interface
 *
 * @property {Function} [onSubmit] - Optional callback function that gets triggered when the login form is submitted.
 * It receives the email and password as arguments.
 */
interface LoginProps {
    onSubmit?: (email: string, password: string) => void;
}

/**
 * Validates if the given string is in a proper email format.
 *
 * This function uses a regular expression to test whether the input string
 * adheres to a common pattern for email addresses. The email format
 * should include a local part, an "@" symbol, a domain, and a top-level domain.
 *
 * @param {string} email - The email string to validate.
 * @returns {boolean} Returns true if the email string is in a valid format, otherwise false.
 */
const validateEmail = (email: string) => {
    return /\S+@\S+\.\S+/.test(email);
}


/**
 * Login component that provides a user interface to allow users to sign in to their account.
 * This component includes input fields for email, password, and password confirmation,
 * as well as client-side validation logic for these fields.
 *
 * @type {React.FC<LoginProps>}
 *
 * @prop {Function} onSubmit - A callback function invoked when the form is submitted.
 *        It receives the email and password as arguments if the form passes validation.
 *
 * @remarks
 * - The email must be a valid email format.
 * - The password must be at least 6 characters long.
 * - The confirm password field must match the password field.
 * - Validation errors are shown alongside the corresponding input fields.
 */
export const Login: React.FC<LoginProps> = ({onSubmit}) => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [errors, setErrors] = useState<{ email?: string; password?: string; confirmPassword?: string }>({});

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();

        const newErrors: { email?: string; password?: string; confirmPassword?: string } = {};

        if (!email) {
            newErrors.email = 'Email is required';
        } else if (!/\S+@\S+\.\S+/.test(email)) {
            newErrors.email = 'Email is invalid';
        }

        if (!password) {
            newErrors.password = 'Password is required';
        } else if (password.length < 6) {
            newErrors.password = 'Password must be at least 6 characters';
        }

        if (!confirmPassword) {
            newErrors.confirmPassword = 'Password confirmation is required';
        } else if (password !== confirmPassword) {
            newErrors.confirmPassword = 'Passwords do not match';
        }

        setErrors(newErrors);

        if (Object.keys(newErrors).length === 0 && onSubmit) {
            onSubmit(email, password);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50">
            <div className="max-w-md w-full space-y-8 p-8 bg-white rounded-lg shadow-md">
                <div>
                    <h2 className="text-center text-3xl font-extrabold text-gray-900">
                        Sign in to your account
                    </h2>
                </div>
                <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
                    <div className="space-y-4">
                        <Input
                            label="Email address"
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            error={errors.email}
                            required
                            placeholder="Enter your email"
                        />
                        <Input
                            label="Password"
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            error={errors.password}
                            required
                            placeholder="Enter your password"
                        />
                        <Input
                            label="Confirm Password"
                            type="password"
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                            error={errors.confirmPassword}
                            required
                            placeholder="Confirm your password"
                        />
                    </div>

                    <div>
                        <button
                            type="submit"
                            className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
                        >
                            Sign in
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

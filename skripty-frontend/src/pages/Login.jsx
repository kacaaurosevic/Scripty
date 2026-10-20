import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await login(email, password);
      navigate("/");
    } catch (err) {
      setError(err.response?.data?.error || "Login failed. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h1>Welcome back</h1>
        <p className="auth-subtitle">Log in to Skripty</p>

        {error && <div className="error-banner">{error}</div>}

        <label>
          Email
            <input type="email" name="email" autoComplete="username" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>

        <label>
          Password
            <input type="password" name="password" autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        </label>

        <button type="submit" disabled={loading}>
          {loading ? "Logging in..." : "Log in"}
        </button>

        <p className="auth-footer">
          No account yet? <Link to="/register">Sign up</Link>
        </p>
      </form>
    </div>
  );
}

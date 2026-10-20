import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand">
        Skripty
      </Link>
      {user && (
        <div className="navbar-user">
          <span>{user.fullName}</span>
          <button onClick={handleLogout} className="link-button">
            Log out
          </button>
        </div>
      )}
    </nav>
  );
}

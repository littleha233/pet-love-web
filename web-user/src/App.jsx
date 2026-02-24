import { Navigate, Route, Routes } from "react-router-dom";
import NavBar from "./components/NavBar.jsx";
import HomePage from "./pages/HomePage.jsx";
import AdoptionPage from "./pages/AdoptionPage.jsx";
import ServicesPage from "./pages/ServicesPage.jsx";

function App() {
  return (
    <div className="app-shell">
      <div className="bg-orb bg-orb-left" />
      <div className="bg-orb bg-orb-right" />
      <NavBar />
      <main className="page-main container">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/adoption" element={<AdoptionPage />} />
          <Route path="/services" element={<ServicesPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <footer className="site-footer container">
        <p>PetLove Web MVP • 温暖治愈 + 专业可信</p>
      </footer>
    </div>
  );
}

export default App;

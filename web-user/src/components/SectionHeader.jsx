function SectionHeader({ eyebrow, title }) {
  return (
    <header className="section-header fade-up">
      <p className="eyebrow">{eyebrow}</p>
      <h2>{title}</h2>
    </header>
  );
}

export default SectionHeader;

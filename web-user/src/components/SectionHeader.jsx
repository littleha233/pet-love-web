function SectionHeader({ eyebrow, title, description }) {
  return (
    <header className="section-header fade-up">
      <p className="eyebrow">{eyebrow}</p>
      <h2>{title}</h2>
      <p>{description}</p>
    </header>
  );
}

export default SectionHeader;

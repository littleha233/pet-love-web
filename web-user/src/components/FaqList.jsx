function FaqList({ items }) {
  return (
    <section className="card faq-panel fade-up">
      <h3>常见问题</h3>
      <div className="faq-list">
        {items.map((item) => (
          <details key={item.q}>
            <summary>{item.q}</summary>
            <p>{item.a}</p>
          </details>
        ))}
      </div>
    </section>
  );
}

export default FaqList;

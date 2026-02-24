function ServiceCard({ service, index = 0 }) {
  return (
    <article
      className="service-card card fade-up"
      style={{ "--delay": `${index * 90}ms` }}
    >
      <h3>{service.title}</h3>
      <p>{service.description}</p>
      <ul>
        {service.items.map((item) => (
          <li key={item}>{item}</li>
        ))}
      </ul>
      <p className="service-price">{service.price}</p>
      <button type="button" className="primary-btn card-btn">
        预约服务
      </button>
    </article>
  );
}

export default ServiceCard;

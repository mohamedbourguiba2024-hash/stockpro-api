CREATE TABLE users (
                       id            BIGINT IDENTITY(1,1) PRIMARY KEY,
                       nom           NVARCHAR(255) NOT NULL,
                       email         VARCHAR(255)  NOT NULL,
                       mot_de_passe  VARCHAR(255)  NOT NULL,
                       role          VARCHAR(255)  NOT NULL,
                       CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE categories (
                            id   BIGINT IDENTITY(1,1) PRIMARY KEY,
                            nom  NVARCHAR(255) NOT NULL,
                            CONSTRAINT uk_categories_nom UNIQUE (nom)
);

CREATE TABLE suppliers (
                           id         BIGINT IDENTITY(1,1) PRIMARY KEY,
                           nom        NVARCHAR(255) NOT NULL,
                           telephone  VARCHAR(255),
                           email      VARCHAR(255),
                           adresse    NVARCHAR(255)
);

CREATE TABLE products (
                          id            BIGINT IDENTITY(1,1) PRIMARY KEY,
                          reference     VARCHAR(255)  NOT NULL,
                          nom           NVARCHAR(255) NOT NULL,
                          description   NVARCHAR(1000),
                          prix          NUMERIC(12,2) NOT NULL,
                          quantite      INT NOT NULL,
                          seuil_alerte  INT NOT NULL,
                          category_id   BIGINT,
                          supplier_id   BIGINT,
                          CONSTRAINT uk_products_reference UNIQUE (reference),
                          CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id),
                          CONSTRAINT fk_products_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

CREATE TABLE stock_movements (
                                 id           BIGINT IDENTITY(1,1) PRIMARY KEY,
                                 product_id   BIGINT NOT NULL,
                                 type         VARCHAR(255) NOT NULL,
                                 quantite     INT NOT NULL,
    [date]       DATETIME2(6) NOT NULL,
    commentaire  NVARCHAR(500),
    user_id      BIGINT NOT NULL,
    CONSTRAINT fk_movements_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_movements_user FOREIGN KEY (user_id) REFERENCES users(id)
    );

CREATE INDEX ix_products_category ON products(category_id);
CREATE INDEX ix_products_supplier ON products(supplier_id);
CREATE INDEX ix_movements_product ON stock_movements(product_id);
CREATE INDEX ix_movements_date ON stock_movements([date]);
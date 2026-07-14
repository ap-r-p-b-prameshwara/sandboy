-- Production Database Initialization Script
-- This script creates all required schemas for the Production environment

-- Create schemas
CREATE SCHEMA IF NOT EXISTS users;
CREATE SCHEMA IF NOT EXISTS credentials;
CREATE SCHEMA IF NOT EXISTS privileges;
CREATE SCHEMA IF NOT EXISTS qris_merchants;
CREATE SCHEMA IF NOT EXISTS qris_transactions;
CREATE SCHEMA IF NOT EXISTS virtual_accounts;
CREATE SCHEMA IF NOT EXISTS top_up_transactions;

-- Users schema tables
CREATE TABLE IF NOT EXISTS users.user (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_email ON users.user(email);

-- Credentials schema tables
CREATE TABLE IF NOT EXISTS credentials.credential (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users.user(id),
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_credential_user_id ON credentials.credential(user_id);
CREATE INDEX IF NOT EXISTS idx_credential_email ON credentials.credential(email);

-- Privileges schema tables
CREATE TABLE IF NOT EXISTS privileges.privilege (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users.user(id),
    feature VARCHAR(100) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, feature)
);

CREATE INDEX IF NOT EXISTS idx_privilege_user_id ON privileges.privilege(user_id);

-- QRIS Merchants schema tables
CREATE TABLE IF NOT EXISTS qris_merchants.merchants (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users.user(id),
    merchant_name VARCHAR(255) NOT NULL,
    nmid VARCHAR(100) UNIQUE,
    phone_number VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT false,
    daily_limit DECIMAL(19, 2),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_merchant_user_id ON qris_merchants.merchants(user_id);

-- QRIS Transactions schema tables
CREATE TABLE IF NOT EXISTS qris_transactions.transactions (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT NOT NULL REFERENCES qris_merchants.merchants(id),
    transaction_id VARCHAR(100) NOT NULL UNIQUE,
    amount DECIMAL(19, 2) NOT NULL,
    customer_reference VARCHAR(255),
    status VARCHAR(20) DEFAULT 'PENDING',
    qris_code TEXT,
    transaction_type VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_transaction_merchant_id ON qris_transactions.transactions(merchant_id);
CREATE INDEX IF NOT EXISTS idx_transaction_status ON qris_transactions.transactions(status);

-- Virtual Accounts schema tables
CREATE TABLE IF NOT EXISTS virtual_accounts.virtual_account (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users.user(id),
    account_number VARCHAR(50) NOT NULL,
    bank_code VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(account_number, bank_code)
);

CREATE INDEX IF NOT EXISTS idx_va_user_id ON virtual_accounts.virtual_account(user_id);
CREATE INDEX IF NOT EXISTS idx_va_account_number ON virtual_accounts.virtual_account(account_number);

-- Top Up Transactions schema tables
CREATE TABLE IF NOT EXISTS top_up_transactions.transaction (
    id BIGSERIAL PRIMARY KEY,
    virtual_account_id BIGINT NOT NULL REFERENCES virtual_accounts.virtual_account(id),
    amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_topup_va_id ON top_up_transactions.transaction(virtual_account_id);
CREATE INDEX IF NOT EXISTS idx_topup_status ON top_up_transactions.transaction(status);
CREATE INDEX IF NOT EXISTS idx_topup_created_at ON top_up_transactions.transaction(created_at);

-- Grant permissions
GRANT ALL PRIVILEGES ON SCHEMA users TO sandbox_user;
GRANT ALL PRIVILEGES ON SCHEMA credentials TO sandbox_user;
GRANT ALL PRIVILEGES ON SCHEMA privileges TO sandbox_user;
GRANT ALL PRIVILEGES ON SCHEMA qris_merchants TO sandbox_user;
GRANT ALL PRIVILEGES ON SCHEMA qris_transactions TO sandbox_user;
GRANT ALL PRIVILEGES ON SCHEMA virtual_accounts TO sandbox_user;
GRANT ALL PRIVILEGES ON SCHEMA top_up_transactions TO sandbox_user;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA users TO sandbox_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA credentials TO sandbox_user;
GRANT ALL PRIVILEGES ON SCHEMA privileges TO sandbox_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA qris_merchants TO sandbox_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA qris_transactions TO sandbox_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA virtual_accounts TO sandbox_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA top_up_transactions TO sandbox_user;

GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA users TO sandbox_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA credentials TO sandbox_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA privileges TO sandbox_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA qris_merchants TO sandbox_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA qris_transactions TO sandbox_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA virtual_accounts TO sandbox_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA top_up_transactions TO sandbox_user;

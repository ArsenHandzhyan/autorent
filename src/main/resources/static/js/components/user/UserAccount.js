import React, { useState, useEffect } from 'react';
import { Card, Table, Typography, Statistic, Alert } from 'antd';
import { WalletOutlined, CreditCardOutlined } from '@ant-design/icons';

const { Title } = Typography;

const UserAccount = () => {
    const [account, setAccount] = useState(null);
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);

    const fetchAccountData = async () => {
        try {
            const response = await fetch('/api/accounts/me');
            const data = await response.json();
            setAccount(data);
        } catch (error) {
            console.error('Ошибка при загрузке данных счета:', error);
        }
        setLoading(false);
    };

    const fetchTransactions = async () => {
        try {
            const response = await fetch('/api/accounts/me/transactions');
            const data = await response.json();
            setTransactions(data);
        } catch (error) {
            console.error('Ошибка при загрузке транзакций:', error);
        }
    };

    useEffect(() => {
        fetchAccountData();
        fetchTransactions();
    }, []);

    const transactionColumns = [
        {
            title: 'Дата',
            dataIndex: 'date',
            key: 'date',
            render: (date) => new Date(date).toLocaleString()
        },
        {
            title: 'Тип операции',
            dataIndex: 'type',
            key: 'type',
            render: (type) => {
                const types = {
                    'RENT_PAYMENT': 'Оплата аренды',
                    'DEPOSIT': 'Пополнение',
                    'REFUND': 'Возврат средств'
                };
                return types[type] || type;
            }
        },
        {
            title: 'Сумма',
            dataIndex: 'amount',
            key: 'amount',
            render: (amount) => {
                const color = amount >= 0 ? 'green' : 'red';
                return <span style={{ color }}>{amount} ₽</span>;
            }
        },
        {
            title: 'Описание',
            dataIndex: 'description',
            key: 'description'
        }
    ];

    if (loading) {
        return <div>Загрузка...</div>;
    }

    return (
        <div className="user-account">
            <Title level={2}>Мой счет</Title>
            
            <div className="account-stats" style={{ display: 'flex', gap: '20px', marginBottom: '20px' }}>
                <Card>
                    <Statistic
                        title="Текущий баланс"
                        value={account?.balance}
                        precision={2}
                        prefix={<WalletOutlined />}
                        suffix="₽"
                    />
                </Card>
                <Card>
                    <Statistic
                        title="Кредитный лимит"
                        value={account?.creditLimit}
                        precision={2}
                        prefix={<CreditCardOutlined />}
                        suffix="₽"
                    />
                </Card>
            </div>

            {account?.balance < 0 && (
                <Alert
                    message="Внимание: отрицательный баланс"
                    description={`Ваш текущий баланс составляет ${account.balance} ₽. Пожалуйста, пополните счет.`}
                    type="warning"
                    showIcon
                    style={{ marginBottom: '20px' }}
                />
            )}

            <Card title="История операций">
                <Table
                    columns={transactionColumns}
                    dataSource={transactions}
                    rowKey="id"
                    pagination={{ pageSize: 10 }}
                />
            </Card>
        </div>
    );
};

export default UserAccount; 